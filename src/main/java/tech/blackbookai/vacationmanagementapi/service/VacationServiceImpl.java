package tech.blackbookai.vacationmanagementapi.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tech.blackbookai.vacationmanagementapi.entity.Vacation;
import tech.blackbookai.vacationmanagementapi.exceptions.VacationInvalidRequestException;
import tech.blackbookai.vacationmanagementapi.exceptions.VacationRequestNotFoundException;
import tech.blackbookai.vacationmanagementapi.model.VacationRequest;
import tech.blackbookai.vacationmanagementapi.model.enums.VacationStatus;
import tech.blackbookai.vacationmanagementapi.repository.VacationRepository;
import tech.blackbookai.vacationmanagementapi.util.DateUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class VacationServiceImpl implements VacationService {

    @Value("${app.max-vacation-per-user}")
    private int vacationLimitPerUser;

    @Autowired
    private VacationRepository vacationRepository;

    /**
     * Creates a vacation request if the user's total approved vacations have not exceeded the maximum allowed vacations per year.
     *
     * @param request The vacation request for a user
     * @return The created vacation request
     * @throws VacationInvalidRequestException If total approved vacations is equal or greater than maximum allowed vacations per year
     * @throws VacationInvalidRequestException If start date is greater than end date
     */
    @Override
    public Vacation createForUser(VacationRequest request) {
        val totalVacations = countNumberOfVacationDaysForUserSinceStartOfYear(request.getAuthor());
        if (vacationLimitPerUser <= totalVacations) {
            log.error("Unable to create new vacation request. User {} has a total of {} approved vacations exceeding limit of {} per year", request.getAuthor(), totalVacations, vacationLimitPerUser);
            throw new VacationInvalidRequestException("User has reached the maximum allowed vacations per year", null);
        }

        if (request.getVacationStartDate().isAfter(request.getVacationEndDate())) {
            log.error("Vacation start date {} cannot be greater than end date {}", request.getVacationStartDate(), request.getVacationEndDate());
            throw new VacationInvalidRequestException("Vacation start date cannot be greater than end date", null);
        }

        val vacation = new Vacation();
        // ID here is passed in the request due to limited time, but ideally, we should query the requesting user via the security context
        vacation.setAuthor(request.getAuthor());
        vacation.setRequestDate(LocalDateTime.now());
        vacation.setStartDate(request.getVacationStartDate());
        vacation.setStartDate(request.getVacationStartDate());
        vacation.setStatus(VacationStatus.PENDING.name());

        log.trace("Creating new vacation request for user {}. Start date: {}, End date: {}", request.getAuthor(), request.getVacationStartDate(), request.getVacationEndDate());
        return vacationRepository.save(vacation);
    }

    /**
     * Returns the vacation requests of a given user. Results will be filtered by status, if provided.
     *
     * @param userId The ID of the author
     * @param statusFilters Optional. The status to filter
     * @return Vacation requests by author
     */
    @Override
    public List<Vacation> getVacationForUserByStatus(Long userId, List<VacationStatus> statusFilters) {
        if (!CollectionUtils.isEmpty(statusFilters)) {
            val statusStringList = statusFilters.stream().map(VacationStatus::name).toList();
            log.trace("Retrieving all vacation requests for user {} filtered by status {}", userId, StringUtils.joinWith(",", statusStringList));
            return vacationRepository.findByAuthorAndStatusIn(userId, statusStringList);
        }
        log.trace("Retrieving all vacation requests for user {}", userId);
        return vacationRepository.findByAuthor(userId);
    }

    /**
     * Returns the remaining vacation days for a user for the current year
     *
     * @return Count of remaining vacation days
     */
    @Override
    public int getRemainingVacationDaysForUser(Long userId) {
        val remainingDays = vacationLimitPerUser - countNumberOfVacationDaysForUserSinceStartOfYear(userId);
        return Math.max(remainingDays, 0);
    }

    /**
     * Returns the vacation requests for all users. Results will be filtered by status, if provided.
     *
     * @param statusFilters Optional. The status to filter
     * @return Vacation requests
     */
    // TODO: If API Security is implemented, @PreAuthorize(hasRole('MANAGER'))
    @Override
    public List<Vacation> getAllVacationRequests(List<VacationStatus> statusFilters) {
        if (!CollectionUtils.isEmpty(statusFilters)) {
            val statusStringList = statusFilters.stream().map(VacationStatus::name).toList();
            log.trace("Retrieving all vacation requests filtered by status {}", StringUtils.joinWith(",", statusStringList));
            return vacationRepository.findByStatusIn(statusStringList);
        }
        return vacationRepository.findAll();
    }

    /**
     * Returns all vacation requests that overlap with another request. Overlap is determined by vacation start date and end date.
     * Examples:
     *  12-28-23 to 01-05-24 overlaps with 01-02-24 to 01-07-24
     *  01-01-24 to 01-05-24 overlaps with 01-05-24 to 01-07-24
     *  01-01-24 to 01-05-24 does not overlap with 01-07-24 to 01-15-24
     *
     * @return Vacation requests
     */
    // TODO: If API Security is implemented, @PreAuthorize(hasRole('MANAGER'))
    @Override
    public Set<Vacation> getOverlappingRequests() {
        val vacationRequests = vacationRepository.findByStatusIn(List.of(VacationStatus.PENDING.name()));

        Set<Vacation> overlappingRequests = new HashSet<>();
        for (val request : vacationRequests) {
            if (!CollectionUtils.isEmpty(findOverlaps(request, vacationRequests))) {
                overlappingRequests.add(request);
                overlappingRequests.addAll(findOverlaps(request, vacationRequests));
            }
        }
        return overlappingRequests;
    }

    /**
     * Updates the status of a vacation request for a given ID
     *
     * @param id The ID of the vacation request
     * @param status The status to update
     */
    @Override
    public void updateRequestStatus(Long id, VacationStatus status) {
        if (status == null) {
            log.error("Unable to update vacation request. Vacation status was not provided.");
            throw new VacationInvalidRequestException("Vacation status is required but not provided", null);
        }
        val vacationRequest = vacationRepository.findById(id).orElseThrow(() -> new VacationRequestNotFoundException("Vacation request does not exist", null));
        vacationRequest.setStatus(status.name());

        // ID is hardcoded due to limited time, but ideally, we should query the requesting user via the security context
        vacationRequest.setResolvedBy(1L);
        vacationRepository.save(vacationRequest);
    }

    private int countNumberOfVacationDaysForUserSinceStartOfYear(Long userId) {
        return vacationRepository
                .countByAuthorAndStatusAndStartDateIsGreaterThanEqual(userId, VacationStatus.APPROVED.name(), LocalDateTime.of(LocalDate.now().getYear(), 1, 1, 0, 0));
    }

    private List<Vacation> findOverlaps(Vacation request, List<Vacation> requestList) {
        return requestList.stream()
                .filter(otherRequest -> !request.equals(otherRequest)
                        && ((DateUtil.isEqualOrBefore(request.getStartDate(), otherRequest.getEndDate()) && DateUtil.isEqualOrAfter(request.getEndDate(), otherRequest.getEndDate()) && DateUtil.isEqualOrAfter(request.getStartDate(), otherRequest.getStartDate()))
                            || (DateUtil.isEqualOrAfter(request.getEndDate(), otherRequest.getStartDate()) && DateUtil.isEqualOrBefore(request.getEndDate(), otherRequest.getEndDate()) && DateUtil.isEqualOrBefore(request.getStartDate(), otherRequest.getStartDate()))))
                .toList();
    }
}

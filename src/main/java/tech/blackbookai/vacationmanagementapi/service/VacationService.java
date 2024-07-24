package tech.blackbookai.vacationmanagementapi.service;

import tech.blackbookai.vacationmanagementapi.entity.Vacation;
import tech.blackbookai.vacationmanagementapi.model.VacationRequest;
import tech.blackbookai.vacationmanagementapi.model.enums.VacationStatus;

import java.util.List;
import java.util.Set;

public interface VacationService {
    Vacation createForUser(VacationRequest request);
    List<Vacation> getVacationForUserByStatus(Long userId, List<VacationStatus> statusFilters);
    int getRemainingVacationDaysForUser(Long userId);
    List<Vacation> getAllVacationRequests(List<VacationStatus> statusFilters);
    Set<Vacation> getOverlappingRequests();
    void updateRequestStatus(Long id, VacationStatus status);
}

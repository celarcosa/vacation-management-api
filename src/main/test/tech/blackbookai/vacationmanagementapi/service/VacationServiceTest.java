package tech.blackbookai.vacationmanagementapi.service;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tech.blackbookai.vacationmanagementapi.entity.Vacation;
import tech.blackbookai.vacationmanagementapi.exceptions.VacationInvalidRequestException;
import tech.blackbookai.vacationmanagementapi.exceptions.VacationRequestNotFoundException;
import tech.blackbookai.vacationmanagementapi.model.VacationRequest;
import tech.blackbookai.vacationmanagementapi.model.enums.VacationStatus;
import tech.blackbookai.vacationmanagementapi.repository.VacationRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
public class VacationServiceTest {

    private static long USER_ID = 1L;

    @InjectMocks
    private VacationServiceImpl service;

    @Mock
    private VacationRepository vacationRepository;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(service, "vacationLimitPerUser", 30);
    }

    @Test
    public void givenVacationCountIsLessThanLimit_whenCreateForUser_thenSuccess() {
        Mockito.when(vacationRepository
                .countByAuthorAndStatusAndStartDateIsGreaterThanEqual(USER_ID, VacationStatus.APPROVED.name(), LocalDateTime.of(LocalDate.now().getYear(), 1, 1, 0, 0)))
                .thenReturn(10);

        val request = new VacationRequest();
        request.setAuthor(USER_ID);
        request.setVacationStartDate(LocalDateTime.now().minusDays(1));
        request.setVacationEndDate(LocalDateTime.now());
        service.createForUser(request);

        ArgumentCaptor<Vacation> captor = ArgumentCaptor.forClass(Vacation.class);
        Mockito.verify(vacationRepository).save(captor.capture());

        assertEquals(VacationStatus.PENDING.name(), captor.getValue().getStatus());
    }

    @Test
    public void givenVacationCountIsEqualToLimit_whenCreateForUser_thenThrowError() {
        Mockito.when(vacationRepository
                        .countByAuthorAndStatusAndStartDateIsGreaterThanEqual(USER_ID, VacationStatus.APPROVED.name(), LocalDateTime.of(LocalDate.now().getYear(), 1, 1, 0, 0)))
                .thenReturn(30);

        val request = new VacationRequest();
        request.setAuthor(USER_ID);
        request.setVacationStartDate(LocalDateTime.now().minusDays(1));
        request.setVacationEndDate(LocalDateTime.now());
        assertThrows(VacationInvalidRequestException.class, () -> service.createForUser(request));
    }

    @Test
    public void givenVacationStartDateIsGreaterThanEndDate_whenCreateForUser_thenThrowError() {
        Mockito.when(vacationRepository
                        .countByAuthorAndStatusAndStartDateIsGreaterThanEqual(USER_ID, VacationStatus.APPROVED.name(), LocalDateTime.of(LocalDate.now().getYear(), 1, 1, 0, 0)))
                .thenReturn(10);

        val request = new VacationRequest();
        request.setAuthor(USER_ID);
        request.setVacationStartDate(LocalDateTime.now().plusDays(1));
        request.setVacationEndDate(LocalDateTime.now());
        assertThrows(VacationInvalidRequestException.class, () -> service.createForUser(request));
    }

    @Test
    public void givenUserIdWithNullStatus_whenGetVacationForUserByStatus_thenReturnAllForUser() {
        Vacation samplePendingVacation = new Vacation();
        samplePendingVacation.setAuthor(USER_ID);
        samplePendingVacation.setRequestDate(LocalDateTime.now());
        samplePendingVacation.setStatus(VacationStatus.PENDING.name());

        Vacation sampleApprovedVacation = new Vacation();
        sampleApprovedVacation.setAuthor(USER_ID);
        sampleApprovedVacation.setRequestDate(LocalDateTime.now());
        sampleApprovedVacation.setStatus(VacationStatus.APPROVED.name());

        val expectedResponse = List.of(samplePendingVacation, sampleApprovedVacation);
        Mockito.when(vacationRepository.findByAuthor(USER_ID))
                .thenReturn(expectedResponse);

        List<Vacation> response = service.getVacationForUserByStatus(USER_ID, null);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void givenUserIdWithPendingStatusFilter_whenGetVacationForUserByStatus_thenReturnOnlyPending() {
        Vacation samplePendingVacation = new Vacation();
        samplePendingVacation.setAuthor(USER_ID);
        samplePendingVacation.setRequestDate(LocalDateTime.now());
        samplePendingVacation.setStatus(VacationStatus.PENDING.name());

        val expectedResponse = List.of(samplePendingVacation);
        Mockito.when(vacationRepository.findByAuthorAndStatusIn(USER_ID, List.of(VacationStatus.PENDING.name())))
                .thenReturn(expectedResponse);

        List<Vacation> response = service.getVacationForUserByStatus(USER_ID, List.of(VacationStatus.PENDING));

        assertTrue(response.stream().anyMatch(vacation -> StringUtils.equals(vacation.getStatus(), VacationStatus.PENDING.name())));
        assertTrue(response.stream().noneMatch(vacation -> StringUtils.equals(vacation.getStatus(), VacationStatus.APPROVED.name())));
    }

    @Test
    public void givenVacationCountIs10_whenGetRemainingVacationDaysForUser_thenReturn20() {
        Mockito.when(vacationRepository
                        .countByAuthorAndStatusAndStartDateIsGreaterThanEqual(USER_ID, VacationStatus.APPROVED.name(), LocalDateTime.of(LocalDate.now().getYear(), 1, 1, 0, 0)))
                .thenReturn(10);
        assertEquals(20, service.getRemainingVacationDaysForUser(USER_ID));
    }

    @Test
    public void givenVacationCountIsNegative_whenGetRemainingVacationDaysForUser_thenReturn0() {
        Mockito.when(vacationRepository
                        .countByAuthorAndStatusAndStartDateIsGreaterThanEqual(USER_ID, VacationStatus.APPROVED.name(), LocalDateTime.of(LocalDate.now().getYear(), 1, 1, 0, 0)))
                .thenReturn(31);
        assertEquals(0, service.getRemainingVacationDaysForUser(USER_ID));
    }

    @Test
    public void givenNullStatusFilter_whenGetAllVacationRequests_thenReturnAll() {
        Vacation samplePendingVacation = new Vacation();
        samplePendingVacation.setAuthor(USER_ID);
        samplePendingVacation.setRequestDate(LocalDateTime.now());
        samplePendingVacation.setStatus(VacationStatus.PENDING.name());

        Vacation sampleApprovedVacation = new Vacation();
        sampleApprovedVacation.setAuthor(USER_ID);
        sampleApprovedVacation.setRequestDate(LocalDateTime.now());
        sampleApprovedVacation.setStatus(VacationStatus.APPROVED.name());

        val expectedResponse = List.of(samplePendingVacation, sampleApprovedVacation);
        Mockito.when(vacationRepository.findAll())
                .thenReturn(expectedResponse);

        List<Vacation> response = service.getAllVacationRequests(null);

        assertEquals(expectedResponse, response);
    }

    @Test
    public void givenPendingStatusFilter_whenGetAllVacationRequests_thenReturnAllPending() {
        Vacation samplePendingVacation = new Vacation();
        samplePendingVacation.setAuthor(USER_ID);
        samplePendingVacation.setRequestDate(LocalDateTime.now());
        samplePendingVacation.setStatus(VacationStatus.PENDING.name());

        val expectedResponse = List.of(samplePendingVacation);
        Mockito.when(vacationRepository.findByStatusIn(List.of(VacationStatus.PENDING.name())))
                .thenReturn(expectedResponse);

        List<Vacation> response = service.getAllVacationRequests(List.of(VacationStatus.PENDING));

        assertEquals(expectedResponse, response);
    }

    @Test
    public void givenAllPendingRequests_whenGetOverlappingRequests_thenReturnAll() {
        Vacation samplePendingVacation1 = new Vacation();
        samplePendingVacation1.setAuthor(USER_ID);
        samplePendingVacation1.setStartDate(LocalDateTime.of(2024, 1, 1, 0, 0));
        samplePendingVacation1.setEndDate(LocalDateTime.of(2024, 1, 5, 0, 0));
        samplePendingVacation1.setStatus(VacationStatus.PENDING.name());

        Vacation samplePendingVacation2 = new Vacation();
        samplePendingVacation2.setAuthor(USER_ID);
        samplePendingVacation2.setStartDate(LocalDateTime.of(2024, 1, 3, 0, 0));
        samplePendingVacation2.setEndDate(LocalDateTime.of(2024, 1, 7, 0, 0));
        samplePendingVacation2.setStatus(VacationStatus.PENDING.name());

        Vacation samplePendingVacation3 = new Vacation();
        samplePendingVacation3.setAuthor(USER_ID);
        samplePendingVacation3.setStartDate(LocalDateTime.of(2024, 2, 2, 0, 0));
        samplePendingVacation3.setEndDate(LocalDateTime.of(2024, 2, 5, 0, 0));
        samplePendingVacation3.setStatus(VacationStatus.PENDING.name());

        Vacation samplePendingVacation4 = new Vacation();
        samplePendingVacation4.setAuthor(USER_ID);
        samplePendingVacation4.setStartDate(LocalDateTime.of(2024, 1, 25, 0, 0));
        samplePendingVacation4.setEndDate(LocalDateTime.of(2024, 2, 2, 0, 0));
        samplePendingVacation4.setStatus(VacationStatus.PENDING.name());

        Vacation samplePendingVacation5 = new Vacation();
        samplePendingVacation5.setAuthor(USER_ID);
        samplePendingVacation5.setStartDate(LocalDateTime.of(2023, 12, 28, 0, 0));
        samplePendingVacation5.setEndDate(LocalDateTime.of(2024, 1, 3, 0, 0));
        samplePendingVacation5.setStatus(VacationStatus.PENDING.name());

        Vacation samplePendingVacation6 = new Vacation();
        samplePendingVacation6.setAuthor(USER_ID);
        samplePendingVacation6.setStartDate(LocalDateTime.of(2024, 5, 1, 0, 0));
        samplePendingVacation6.setEndDate(LocalDateTime.of(2024, 5, 3, 0, 0));
        samplePendingVacation6.setStatus(VacationStatus.PENDING.name());

        val expectedResponse = List.of(samplePendingVacation1, samplePendingVacation2, samplePendingVacation3, samplePendingVacation4, samplePendingVacation5, samplePendingVacation6);
        Mockito.when(vacationRepository.findByStatusIn(List.of(VacationStatus.PENDING.name())))
                .thenReturn(expectedResponse);

        Set<Vacation> response = service.getOverlappingRequests();

        assertEquals(5, response.size());
        assertTrue(response.stream().anyMatch(vacation -> vacation.equals(samplePendingVacation1)));
        assertTrue(response.stream().anyMatch(vacation -> vacation.equals(samplePendingVacation2)));
        assertTrue(response.stream().anyMatch(vacation -> vacation.equals(samplePendingVacation3)));
        assertTrue(response.stream().anyMatch(vacation -> vacation.equals(samplePendingVacation4)));
        assertTrue(response.stream().anyMatch(vacation -> vacation.equals(samplePendingVacation5)));
    }

    @Test
    public void givenApprovedStatus_whenUpdateRequestStatus_thenSuccess() {
        Vacation samplePendingVacation = new Vacation();
        samplePendingVacation.setAuthor(USER_ID);
        samplePendingVacation.setRequestDate(LocalDateTime.now());
        samplePendingVacation.setStatus(VacationStatus.PENDING.name());

        Mockito.when(vacationRepository.findById(1L))
                .thenReturn(Optional.of(samplePendingVacation));

        service.updateRequestStatus(1L, VacationStatus.APPROVED);
        ArgumentCaptor<Vacation> captor = ArgumentCaptor.forClass(Vacation.class);
        Mockito.verify(vacationRepository).save(captor.capture());

        assertEquals(VacationStatus.APPROVED.name(), captor.getValue().getStatus());
    }

    @Test
    public void givenIdDoesNotExist_whenUpdateRequestStatus_thenThrowError() {
        Mockito.when(vacationRepository.findById(2L))
                .thenThrow(new VacationRequestNotFoundException("not-found", null));

        assertThrows(VacationRequestNotFoundException.class, () -> service.updateRequestStatus(2L, VacationStatus.APPROVED));
    }

    @Test
    public void givenNullStatus_whenUpdateRequestStatus_thenThrowError() {
        assertThrows(VacationInvalidRequestException.class, () -> service.updateRequestStatus(1L, null));
    }
}

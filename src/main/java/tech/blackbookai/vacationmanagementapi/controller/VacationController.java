package tech.blackbookai.vacationmanagementapi.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import tech.blackbookai.vacationmanagementapi.entity.Vacation;
import tech.blackbookai.vacationmanagementapi.model.VacationRemainingResponse;
import tech.blackbookai.vacationmanagementapi.model.VacationRequest;
import tech.blackbookai.vacationmanagementapi.model.enums.VacationStatus;
import tech.blackbookai.vacationmanagementapi.service.VacationService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/vacations")
public class VacationController {

    @Autowired
    private VacationService vacationService;

    @PostMapping("/requests")
    private ResponseEntity<Vacation> submitVacationRequest(@RequestBody @Valid VacationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vacationService.createForUser(request));
    }

    @GetMapping("/{userId}/requests")
    private ResponseEntity<List<Vacation>> getUserVacationRequests(@PathVariable Long userId,
                                                                   @RequestParam(required = false) List<String> status) {
        if (!CollectionUtils.isEmpty(status)) {
            return ResponseEntity.ok(vacationService.getVacationForUserByStatus(userId, status.stream().map(VacationStatus::find).toList()));
        }
        return ResponseEntity.ok(vacationService.getVacationForUserByStatus(userId, null));
    }

    @GetMapping("/{userId}/remaining")
    private ResponseEntity<VacationRemainingResponse> getUserRemainingVacationDays(@PathVariable Long userId) {
        return ResponseEntity.ok(new VacationRemainingResponse(vacationService.getRemainingVacationDaysForUser(userId)));
    }

    @GetMapping("/requests")
    private ResponseEntity<List<Vacation>> getAllVacationRequests(@RequestParam(required = false) List<String> status) {
        if (!CollectionUtils.isEmpty(status)) {
            return ResponseEntity.ok(vacationService.getAllVacationRequests(status.stream().map(VacationStatus::find).toList()));
        }
        return ResponseEntity.ok(vacationService.getAllVacationRequests(null));
    }

    @GetMapping("/overlaps")
    private ResponseEntity<Set<Vacation>> getOverlappingRequests() {
        return ResponseEntity.ok(vacationService.getOverlappingRequests());
    }

    @PutMapping("/{id}/approve")
    private ResponseEntity<Void> approveVacationRequest(@PathVariable Long id) {
        vacationService.updateRequestStatus(id, VacationStatus.APPROVED);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .build();
    }

    @PutMapping("/{id}/reject")
    private ResponseEntity<Void> rejectVacationRequest(@PathVariable Long id) {
        vacationService.updateRequestStatus(id, VacationStatus.REJECTED);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .build();
    }
}

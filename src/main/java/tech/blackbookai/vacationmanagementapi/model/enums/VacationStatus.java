package tech.blackbookai.vacationmanagementapi.model.enums;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public enum VacationStatus {
    PENDING,
    APPROVED,
    REJECTED;

    public static VacationStatus find(String statusString) {
        return Arrays.stream(VacationStatus.values())
                .filter(status -> StringUtils.equals(statusString, status.name()))
                .findFirst()
                .orElse(null);
    }
}

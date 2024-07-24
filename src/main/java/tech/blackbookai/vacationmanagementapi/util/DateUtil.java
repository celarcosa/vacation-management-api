package tech.blackbookai.vacationmanagementapi.util;

import java.time.LocalDateTime;

public final class DateUtil {

    public static boolean isEqualOrAfter(LocalDateTime date1, LocalDateTime date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.isEqual(date2) || date1.isAfter(date2);
    }

    public static boolean isEqualOrBefore(LocalDateTime date1, LocalDateTime date2) {
        if (date1 == null || date2 == null) {
            return false;
        }
        return date1.isEqual(date2) || date1.isBefore(date2);
    }
}

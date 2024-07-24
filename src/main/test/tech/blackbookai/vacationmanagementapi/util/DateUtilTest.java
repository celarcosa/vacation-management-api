package tech.blackbookai.vacationmanagementapi.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class DateUtilTest {

    @Test
    public void givenDate1OlderThanDate2_whenIsEqualOrAfter_thenReturnFalse() {
        LocalDateTime date1 = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime date2 = LocalDateTime.of(2024, 1, 1, 0, 0);
        assertFalse(DateUtil.isEqualOrAfter(date1, date2));
    }

    @Test
    public void givenDate1NewerThanDate2_whenIsEqualOrAfter_thenReturnTrue() {
        LocalDateTime date1 = LocalDateTime.of(2024, 1, 2, 1, 0);
        LocalDateTime date2 = LocalDateTime.of(2024, 1, 1, 0, 0);
        assertTrue(DateUtil.isEqualOrAfter(date1, date2));
    }

    @Test
    public void givenDate1EqualToDate2_whenIsEqualOrAfter_thenReturnTrue() {
        LocalDateTime date1 = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime date2 = LocalDateTime.of(2024, 1, 1, 0, 0);
        assertTrue(DateUtil.isEqualOrAfter(date1, date2));
    }

    @Test
    public void givenDate1OlderThanDate2_whenIsEqualOrBefore_thenReturnTrue() {
        LocalDateTime date1 = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime date2 = LocalDateTime.of(2024, 1, 1, 0, 0);
        assertTrue(DateUtil.isEqualOrBefore(date1, date2));
    }

    @Test
    public void givenDate1NewerThanDate2_whenIsEqualOrBefore_thenReturnFalse() {
        LocalDateTime date1 = LocalDateTime.of(2024, 1, 2, 1, 0);
        LocalDateTime date2 = LocalDateTime.of(2024, 1, 1, 0, 0);
        assertFalse(DateUtil.isEqualOrBefore(date1, date2));
    }

    @Test
    public void givenDate1EqualToDate2_whenIsEqualOrBefore_thenReturnTrue() {
        LocalDateTime date1 = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime date2 = LocalDateTime.of(2024, 1, 1, 0, 0);
        assertTrue(DateUtil.isEqualOrBefore(date1, date2));
    }
}

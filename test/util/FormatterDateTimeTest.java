package util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.UnsupportedTemporalTypeException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression for the post-signup Dashboard crash:
 * {@code UnsupportedTemporalTypeException: Unsupported field: DayOfWeek}.
 *
 * <p>Root cause was formatting a day-of-week pattern ("EEEE") against a bare
 * {@link LocalTime} (which has no date) in the top bar. The fix formats against
 * a date-bearing {@link LocalDateTime} via {@link Formatter#dayAndTime}.</p>
 */
class FormatterDateTimeTest {

    @Test
    void dayAndTimeFormatsDateBearingTemporalWithoutThrowing() {
        String s = assertDoesNotThrow(() -> Formatter.dayAndTime(LocalDateTime.of(2026, 6, 3, 9, 30)));
        assertTrue(s.contains("·"));
        assertTrue(s.contains("09:30"));
    }

    @Test
    void dayAndTimeIsNullSafe() {
        assertEquals("—", Formatter.dayAndTime(null));
    }

    @Test
    void dayOfWeekOnLocalTimeReproducesTheOriginalBug() {
        // Documents exactly what crashed: day-of-week against a time-only value.
        assertThrows(UnsupportedTemporalTypeException.class,
                () -> LocalTime.of(9, 30).format(DateTimeFormatter.ofPattern("EEEE")));
    }
}

package util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Formatter — pure utility for turning data into human-readable strings.
 *
 * <p>Role: views never call {@code DateTimeFormatter} or string-build dates
 * themselves. They ask Formatter. This keeps display logic consistent and
 * easy to localize later.</p>
 */
public final class Formatter {

    private static final DateTimeFormatter HUMAN_DATE = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter SHORT_TIME = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
    private static final DateTimeFormatter DAY_AND_TIME = DateTimeFormatter.ofPattern("EEEE · HH:mm", Locale.ENGLISH);

    private Formatter() {}

    /**
     * Day-of-week + clock, e.g. "Wednesday · 09:30".
     *
     * <p>Takes a {@link LocalDateTime} on purpose: the {@code EEEE} (day-of-week)
     * field requires a date. Formatting day-of-week against a bare
     * {@link java.time.LocalTime} throws
     * {@link java.time.temporal.UnsupportedTemporalTypeException} — the exact
     * crash this method exists to prevent. Null-safe.</p>
     */
    public static String dayAndTime(LocalDateTime when) {
        return when == null ? "—" : when.format(DAY_AND_TIME);
    }

    /** Formats a date as e.g. "Mar 14, 2026". Null-safe. */
    public static String date(LocalDate d) {
        return d == null ? "—" : d.format(HUMAN_DATE);
    }

    /** Formats clock time like "14:32". */
    public static String time(LocalDateTime t) {
        return t == null ? "—" : t.format(SHORT_TIME);
    }

    /** Turns a Duration into a "2h15" / "45m" string. */
    public static String duration(Duration d) {
        if (d == null) return "—";
        long minutes = d.toMinutes();
        long hours   = minutes / 60;
        long rest    = minutes % 60;
        if (hours <= 0) return rest + "m";
        if (rest == 0) return hours + "h";
        return hours + "h" + String.format("%02d", rest);
    }

    /** "1h20" style format from raw minute count. */
    public static String minutes(int totalMinutes) {
        return duration(Duration.ofMinutes(totalMinutes));
    }

    /** Renders a 0..1 ratio as a clean percentage like "74%". */
    public static String percent(double ratio) {
        return Math.round(ratio * 100) + "%";
    }
}

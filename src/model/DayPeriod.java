package model;

import java.time.LocalTime;

/**
 * DayPeriod — morning / afternoon / evening, with simple clock boundaries so
 * the temporal engine can map "now" to a part of the day.
 */
public enum DayPeriod {
    MORNING("Morning"),
    AFTERNOON("Afternoon"),
    EVENING("Evening");

    private final String label;
    DayPeriod(String label) { this.label = label; }
    public String label() { return label; }

    /** Maps a clock time to a period: morning <12:00, afternoon <18:00, else evening. */
    public static DayPeriod of(LocalTime time) {
        int h = time.getHour();
        if (h < 12) return MORNING;
        if (h < 18) return AFTERNOON;
        return EVENING;
    }
}

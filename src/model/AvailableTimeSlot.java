package model;

/**
 * AvailableTimeSlot — how much uninterrupted time the user has right now.
 * Drives the Coach's duration-fit reasoning.
 */
public enum AvailableTimeSlot {
    QUARTER(15, "15 min"),
    HALF(30, "30 min"),
    HOUR(60, "60 min"),
    DEEP(120, "120 min");

    private final int minutes;
    private final String label;

    AvailableTimeSlot(int minutes, String label) {
        this.minutes = minutes;
        this.label = label;
    }

    public int minutes() { return minutes; }
    public String label() { return label; }
}

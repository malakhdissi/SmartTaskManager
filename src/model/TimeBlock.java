package model;

import java.time.LocalTime;

/**
 * TimeBlock — a recurring window in the user's day with an expected energy
 * level (e.g. "09:00–11:00, HIGH, deep-work morning"). The temporal engine
 * fits tasks into these blocks.
 */
public class TimeBlock {

    private final String id;
    private final String userId;
    private LocalTime startTime;
    private LocalTime endTime;
    private EnergyLevel energyLevel;
    private int availableMinutes;
    private String label;

    public TimeBlock(String id, String userId, LocalTime startTime, LocalTime endTime,
                     EnergyLevel energyLevel, int availableMinutes, String label) {
        this.id = id;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.energyLevel = energyLevel;
        this.availableMinutes = availableMinutes;
        this.label = label;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public EnergyLevel getEnergyLevel() { return energyLevel; }
    public int getAvailableMinutes() { return availableMinutes; }
    public String getLabel() { return label; }

    public void setStartTime(LocalTime t) { this.startTime = t; }
    public void setEndTime(LocalTime t) { this.endTime = t; }
    public void setEnergyLevel(EnergyLevel e) { this.energyLevel = e; }
    public void setAvailableMinutes(int m) { this.availableMinutes = m; }
    public void setLabel(String l) { this.label = l; }

    /** True if the given clock time falls inside this block. */
    public boolean contains(LocalTime t) {
        return !t.isBefore(startTime) && t.isBefore(endTime);
    }

    /** Which part of the day this block starts in. */
    public DayPeriod period() { return DayPeriod.of(startTime); }
}

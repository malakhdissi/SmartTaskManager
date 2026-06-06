package model;

/**
 * HabitEntry — one tracked habit (sleep, study streak, screen time, etc.).
 *
 * <p>Used by the Habit Tracking screen. Stored progress is a 0..1 ratio
 * against the target; consistency is the % of days the user kept the habit
 * over the last N days. Both shape calm, non-judgmental visualizations.</p>
 */
public class HabitEntry {

    private final String id;
    private final String name;
    private final double progress;      // 0..1
    private final double consistency;   // 0..1
    private final String accent;        // tag color: success / warning / intel / primary

    public HabitEntry(String id, String name, double progress, double consistency, String accent) {
        this.id = id;
        this.name = name;
        this.progress = progress;
        this.consistency = consistency;
        this.accent = accent;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getProgress() { return progress; }
    public double getConsistency() { return consistency; }
    public String getAccent() { return accent; }
}

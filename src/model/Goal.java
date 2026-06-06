package model;

import java.time.LocalDate;

/**
 * Goal — the user's "north star" outcome (e.g. "Become a software engineer").
 *
 * <p>Tasks link to a goal (via {@link Task#getGoalId()}) and each carries a
 * contribution weight; goal <em>progress</em> is computed from completed linked
 * tasks by {@code GoalProgressService} — the stored {@link #progress} field is a
 * persisted cache, not the display source of truth.</p>
 */
public class Goal {

    private final String id;
    private String title;
    private String description;
    private GoalCategory category;
    private int importance;            // 1..5
    private LocalDate targetDate;      // nullable
    private GoalStatus status;
    /** 0..1 — persisted cache; live progress is computed from linked tasks. */
    private double progress;
    /** True if this is the goal the recommendation/coach engine optimizes for. */
    private boolean active;

    public Goal(String id, String title, String description, GoalCategory category, int importance,
                LocalDate targetDate, GoalStatus status, double progress, boolean active) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category == null ? GoalCategory.OTHER : category;
        this.importance = importance;
        this.targetDate = targetDate;
        this.status = status == null ? GoalStatus.ACTIVE : status;
        this.progress = progress;
        this.active = active;
    }

    /** Backward-compatible constructor with sensible defaults. */
    public Goal(String id, String title, String description, double progress, boolean active) {
        this(id, title, description, GoalCategory.OTHER, 3, null, GoalStatus.ACTIVE, progress, active);
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public GoalCategory getCategory() { return category; }
    public int getImportance() { return importance; }
    public LocalDate getTargetDate() { return targetDate; }
    public GoalStatus getStatus() { return status; }
    public double getProgress() { return progress; }
    public boolean isActive() { return active; }

    public void setTitle(String title)             { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCategory(GoalCategory category) { this.category = category; }
    public void setImportance(int importance)      { this.importance = importance; }
    public void setTargetDate(LocalDate targetDate){ this.targetDate = targetDate; }
    public void setStatus(GoalStatus status)       { this.status = status; }
    public void setProgress(double progress)       { this.progress = progress; }
    public void setActive(boolean active)          { this.active = active; }
}

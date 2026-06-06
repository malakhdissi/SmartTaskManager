package model;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * FocusSession — a recorded Deep Work block (timer + linked task).
 *
 * <p>The Productivity Timeline and Insights screens read these to build the
 * "what did the user actually focus on" story — separate from "what tasks
 * did they mark done", which is a different signal.</p>
 */
public class FocusSession {

    private final String id;
    private final String taskId;
    private final String taskTitle; // denormalized for fast UI rendering
    private final LocalDateTime startedAt;
    private final Duration plannedDuration;
    private final Duration actualDuration;
    private final boolean completed;

    public FocusSession(String id, String taskId, String taskTitle,
                        LocalDateTime startedAt, Duration plannedDuration,
                        Duration actualDuration, boolean completed) {
        this.id = id;
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.startedAt = startedAt;
        this.plannedDuration = plannedDuration;
        this.actualDuration = actualDuration;
        this.completed = completed;
    }

    public String getId() { return id; }
    public String getTaskId() { return taskId; }
    public String getTaskTitle() { return taskTitle; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public Duration getPlannedDuration() { return plannedDuration; }
    public Duration getActualDuration() { return actualDuration; }
    public boolean isCompleted() { return completed; }
}

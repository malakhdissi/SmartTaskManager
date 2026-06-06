package model;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Task — central domain entity of the product.
 *
 * <p>A Task captures: identity, what to do (title/description), how to
 * prioritize it (priority/score), when it must be done (deadline), what
 * shape of work it is (type), and its lifecycle (status).</p>
 *
 * <p>Scalability note: this is a plain POJO. It contains no UI code, no DAO
 * code, no JSON code. That separation means the same model object will work
 * unchanged when we later add JPA persistence, REST serialization, or AI
 * scoring features.</p>
 */
public class Task {

    private final String id;
    private String title;
    private String description;
    private Priority priority;
    private TaskStatus status;
    private TaskType type;
    private LocalDate deadline;
    private Duration estimatedDuration;
    /** Score is computed by a ScoringStrategy — not by the UI. */
    private double score;
    /** 0..1 — how much this task contributes to the user's primary goal. */
    private double goalContribution;
    /** How the task relates to time. May be null for legacy tasks — see {@link #getTemporalType()}. */
    private TaskTemporalType temporalType;
    /** Id of the goal this task contributes to, or null if unlinked. */
    private String goalId;

    public Task(String id,
                String title,
                String description,
                Priority priority,
                TaskStatus status,
                TaskType type,
                LocalDate deadline,
                Duration estimatedDuration,
                double score,
                double goalContribution) {
        this.id = Objects.requireNonNull(id);
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.type = type;
        this.deadline = deadline;
        this.estimatedDuration = estimatedDuration;
        this.score = score;
        this.goalContribution = goalContribution;
    }

    /* -------- accessors (kept verbose for clarity to beginners) -------- */
    public String getId()                  { return id; }
    public String getTitle()               { return title; }
    public String getDescription()         { return description; }
    public Priority getPriority()          { return priority; }
    public TaskStatus getStatus()          { return status; }
    public TaskType getType()              { return type; }
    public LocalDate getDeadline()         { return deadline; }
    public Duration getEstimatedDuration() { return estimatedDuration; }
    public double getScore()               { return score; }
    public double getGoalContribution()    { return goalContribution; }

    /**
     * The temporal type, never null: falls back to a value derived from the
     * cognitive {@link TaskType} when one was never explicitly assigned. Keeps
     * legacy tasks working with the temporal engine.
     */
    public TaskTemporalType getTemporalType() {
        return temporalType != null ? temporalType : TaskTemporalType.deriveFrom(type);
    }

    /** The explicitly-set temporal type, or null if it should be derived. Used by persistence. */
    public TaskTemporalType getExplicitTemporalType() { return temporalType; }

    /** Id of the linked goal, or null if this task isn't linked to a goal. */
    public String getGoalId() { return goalId; }

    public void setTitle(String title)                 { this.title = title; }
    public void setDescription(String description)     { this.description = description; }
    public void setPriority(Priority priority)         { this.priority = priority; }
    public void setStatus(TaskStatus status)           { this.status = status; }
    public void setType(TaskType type)                 { this.type = type; }
    public void setDeadline(LocalDate deadline)        { this.deadline = deadline; }
    public void setEstimatedDuration(Duration d)       { this.estimatedDuration = d; }
    public void setScore(double score)                 { this.score = score; }
    public void setGoalContribution(double v)          { this.goalContribution = v; }
    public void setTemporalType(TaskTemporalType t)    { this.temporalType = t; }
    public void setGoalId(String goalId)               { this.goalId = goalId; }

    /** Tasks compare/hash by id — title may change, identity does not. */
    @Override public boolean equals(Object o) {
        if (!(o instanceof Task)) return false;
        return id.equals(((Task) o).id);
    }
    @Override public int hashCode() { return id.hashCode(); }
    @Override public String toString() { return "Task{" + id + ", " + title + "}"; }
}

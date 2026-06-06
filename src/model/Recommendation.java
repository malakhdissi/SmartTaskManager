package model;

/**
 * Recommendation — a "next action" suggested by the engine, with reasoning.
 *
 * <p>Reasoning is *first-class*: a recommendation without an explanation is
 * a dark pattern. The UI always shows the {@code reason} alongside the
 * suggestion, so the user understands why the system surfaced it.</p>
 */
public class Recommendation {

    private final Task task;
    private final String reason;         // "Highest goal contribution this week"
    private final double confidence;     // 0..1 (used to render a soft indicator)

    public Recommendation(Task task, String reason, double confidence) {
        this.task = task;
        this.reason = reason;
        this.confidence = confidence;
    }

    public Task getTask() { return task; }
    public String getReason() { return reason; }
    public double getConfidence() { return confidence; }
}

package model;

/**
 * TaskTemporalType — how a task relates to <em>time</em>, independent of its
 * cognitive {@link TaskType}.
 *
 * <ul>
 *   <li>{@link #FIXED_TIME} — must happen at a specific clock time (meeting, exam).</li>
 *   <li>{@link #FLEXIBLE} — can be done whenever there is a gap (email, reading).</li>
 *   <li>{@link #DEEP_WORK} — needs a protected, high-energy focus window.</li>
 *   <li>{@link #INDIVISIBLE} — must be done in one uninterrupted sitting; never split
 *       across small blocks (e.g. doctoral research).</li>
 * </ul>
 */
public enum TaskTemporalType {
    FIXED_TIME("Fixed time"),
    FLEXIBLE("Flexible"),
    DEEP_WORK("Deep work"),
    INDIVISIBLE("Indivisible");

    private final String label;
    TaskTemporalType(String label) { this.label = label; }
    public String label() { return label; }

    /**
     * Backward-compatible default: derives a sensible temporal type from a
     * task's existing cognitive {@link TaskType} when none was explicitly set.
     */
    public static TaskTemporalType deriveFrom(TaskType type) {
        if (type == null) return FLEXIBLE;
        return switch (type) {
            case DEEP_WORK -> DEEP_WORK;
            case LEARNING, CREATIVE -> DEEP_WORK;
            case REVIEW -> FLEXIBLE;
            case ADMIN, SHORT_FOCUS, OTHER -> FLEXIBLE;
        };
    }
}

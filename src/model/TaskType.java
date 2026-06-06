package model;

/**
 * TaskType — describes the *mental shape* of work, not just its category.
 *
 * <p>Used by the recommendation engine and the Add Task form to give
 * context-aware suggestions (e.g. DEEP_WORK → recommend 90 minutes of
 * uninterrupted focus). This is what makes the product more than a todo app.</p>
 */
public enum TaskType {
    DEEP_WORK("Deep Work"),
    SHORT_FOCUS("Short Focus"),
    ADMIN("Admin"),
    LEARNING("Learning"),
    REVIEW("Review"),
    CREATIVE("Creative"),
    OTHER("Other");

    private final String label;
    TaskType(String label) { this.label = label; }
    public String label() { return label; }
}

package model;

/**
 * TaskStatus — lifecycle states for a task.
 *
 * <p>States are deliberately small: too many statuses become a checklist
 * burden that increases cognitive load. We start with four and may add
 * BLOCKED / SCHEDULED later if the product needs them.</p>
 */
public enum TaskStatus {
    TODO("To do"),
    IN_PROGRESS("In progress"),
    DONE("Done"),
    SKIPPED("Skipped");

    private final String label;
    TaskStatus(String label) { this.label = label; }
    public String label() { return label; }
}

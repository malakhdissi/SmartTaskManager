package model;

/** Lifecycle of a goal. */
public enum GoalStatus {
    ACTIVE("Active"),
    ON_HOLD("On hold"),
    ACHIEVED("Achieved"),
    ARCHIVED("Archived");

    private final String label;
    GoalStatus(String label) { this.label = label; }
    public String label() { return label; }
}

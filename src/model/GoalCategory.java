package model;

/** Life area a goal belongs to (used for grouping and, later, value-alignment). */
public enum GoalCategory {
    CAREER("Career"),
    LEARNING("Learning"),
    HEALTH("Health"),
    FINANCE("Finance"),
    PERSONAL("Personal"),
    OTHER("Other");

    private final String label;
    GoalCategory(String label) { this.label = label; }
    public String label() { return label; }
}

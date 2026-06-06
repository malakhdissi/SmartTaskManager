package model;

/**
 * Priority — calm, ethical task urgency scale.
 *
 * <p>We intentionally avoid "URGENT!!!" / aggressive red overload semantics.
 * HIGH is the strongest level a user can set; the recommendation engine
 * combines priority with deadlines + scoring to decide what to surface.</p>
 */
public enum Priority {
    LOW("Low"),
    MEDIUM("Medium"),
    HIGH("High");

    private final String label;

    Priority(String label) { this.label = label; }

    public String label() { return label; }
}

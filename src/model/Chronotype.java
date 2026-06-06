package model;

/**
 * Chronotype — the user's natural daily rhythm, used to bias deep-work
 * recommendations toward the period they are sharpest.
 */
public enum Chronotype {
    EARLY_BIRD("Early bird"),
    INTERMEDIATE("Intermediate"),
    NIGHT_OWL("Night owl");

    private final String label;
    Chronotype(String label) { this.label = label; }
    public String label() { return label; }
}

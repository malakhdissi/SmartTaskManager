package model;

/**
 * Persona — adaptive focus mode the user is currently operating under.
 *
 * <p>We deliberately use neutral, non-medical labels in the UI (no ADHD,
 * no clinical terms). Persona drives nudges, default durations, and
 * recommendation thresholds — never shame.</p>
 */
public enum Persona {
    ANTI_PROCRASTINATION("Anti-Procrastination"),
    ANTI_SCROLLING("Anti-Scrolling"),
    DEEP_WORK("Deep Work"),
    SHORT_FOCUS("Short Focus"),
    BALANCED("Balanced");

    private final String label;
    Persona(String label) { this.label = label; }
    public String label() { return label; }
}

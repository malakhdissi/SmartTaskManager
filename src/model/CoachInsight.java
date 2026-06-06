package model;

/**
 * CoachInsight — a real, evidence-backed observation. Never fabricated:
 * {@code realData=false} signals an honest "not enough data yet" note.
 */
public record CoachInsight(Kind kind, String message, boolean realData) {

    public enum Kind {
        PROCRASTINATION_RISK,
        FOCUS_RISK,
        DEADLINE_PRESSURE,
        OVERLOAD,
        CONFIDENCE,
        INFO
    }
}

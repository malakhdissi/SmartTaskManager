package model;

/**
 * DistractionEntry — minutes spent on a distraction source today.
 *
 * <p>Display tone is intentionally calm: never shame the user. The screen
 * frames each row with a "gradual reduction plan" rather than a guilt label.</p>
 */
public class DistractionEntry {

    private final String source;       // "Instagram", "YouTube", "Gaming"
    private final int minutesToday;
    private final int reductionGoalMinutes; // desired cap

    public DistractionEntry(String source, int minutesToday, int reductionGoalMinutes) {
        this.source = source;
        this.minutesToday = minutesToday;
        this.reductionGoalMinutes = reductionGoalMinutes;
    }

    public String getSource() { return source; }
    public int getMinutesToday() { return minutesToday; }
    public int getReductionGoalMinutes() { return reductionGoalMinutes; }

    /** Progress toward the reduction goal — capped to 0..1. */
    public double progressRatio() {
        if (reductionGoalMinutes <= 0) return 0d;
        double r = (double) minutesToday / (double) reductionGoalMinutes;
        return Math.max(0d, Math.min(1d, r));
    }
}

package model;

/**
 * LeaderboardEntry — one row of the ethical, opt-in competition screen.
 *
 * <p>We expose only positive signals (focus minutes, streak, consistency).
 * We do NOT publish failure metrics, missed days, distraction time, or
 * comparative shaming language.</p>
 */
public class LeaderboardEntry {

    private final String displayName;
    private final int focusMinutesThisWeek;
    private final int streakDays;
    private final boolean isYou;

    public LeaderboardEntry(String displayName, int focusMinutesThisWeek, int streakDays, boolean isYou) {
        this.displayName = displayName;
        this.focusMinutesThisWeek = focusMinutesThisWeek;
        this.streakDays = streakDays;
        this.isYou = isYou;
    }

    public String getDisplayName() { return displayName; }
    public int getFocusMinutesThisWeek() { return focusMinutesThisWeek; }
    public int getStreakDays() { return streakDays; }
    public boolean isYou() { return isYou; }
}

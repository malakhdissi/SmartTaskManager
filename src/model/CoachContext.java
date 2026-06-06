package model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * CoachContext — an immutable snapshot of everything the Coach reasons over,
 * assembled from real data + the user's selected energy and available time.
 *
 * <p>Missing-data flags ({@code goalsAvailable}, {@code deepWorkAvailable}) let
 * lenses degrade honestly instead of inventing signals. Nullable fields are
 * genuinely absent, not faked.</p>
 */
public record CoachContext(
        LocalDateTime now,
        DayPeriod period,
        EnergyLevel energy,
        int availableMinutes,
        List<Task> activeTasks,
        int overdue,
        int completed,
        String activeGoalId,           // null if no active goal
        String activeGoalTitle,        // null if no active goal
        Integer activeGoalProgress,    // computed from linked tasks; null if no active goal
        Long deepWorkMinutes,          // null if not tracked
        DayPeriod bestDeepWorkPeriod,  // from temporal profile (may be a default)
        boolean goalsAvailable,
        boolean deepWorkAvailable,
        boolean hasTasks) {}

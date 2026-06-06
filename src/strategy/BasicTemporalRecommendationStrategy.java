package strategy;

import model.DayPeriod;
import model.EnergyLevel;
import model.Recommendation;
import model.Task;
import model.TaskStatus;
import model.TaskTemporalType;
import model.TemporalProfile;
import model.TimeBlock;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

/**
 * BasicTemporalRecommendationStrategy — transparent heuristic that matches task
 * difficulty to current energy and respects temporal constraints.
 *
 * <p>Core rules:
 * <ul>
 *   <li><b>FIXED_TIME</b> tasks due today are surfaced first — they can't move.</li>
 *   <li><b>INDIVISIBLE</b> tasks are only recommended when the current block has
 *       enough uninterrupted minutes; they are never suggested for a short gap.</li>
 *   <li><b>DEEP_WORK</b>/INDIVISIBLE need HIGH energy; they're boosted in
 *       high-energy windows and suppressed in fatigue periods.</li>
 *   <li><b>FLEXIBLE</b> tasks are good uses of low-energy time.</li>
 * </ul>
 */
public class BasicTemporalRecommendationStrategy implements TemporalRecommendationStrategy {

    /** Minutes assumed available when "now" falls outside any defined block. */
    private static final int DEFAULT_GAP_MINUTES = 25;

    @Override
    public Optional<Recommendation> recommendNow(List<Task> tasks,
                                                 TemporalProfile profile,
                                                 List<TimeBlock> blocks,
                                                 LocalDateTime now) {
        if (tasks == null || tasks.isEmpty()) return Optional.empty();

        DayPeriod period = DayPeriod.of(now.toLocalTime());
        TimeBlock current = currentBlock(blocks, now.toLocalTime());
        EnergyLevel available = current != null ? current.getEnergyLevel()
                : (profile != null ? profile.energyFor(period) : EnergyLevel.MEDIUM);
        int availableMinutes = current != null ? current.getAvailableMinutes() : DEFAULT_GAP_MINUTES;

        Task best = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        String bestReason = "";

        for (Task t : tasks) {
            if (t.getStatus() == TaskStatus.DONE || t.getStatus() == TaskStatus.SKIPPED) continue;

            TaskTemporalType temporal = t.getTemporalType();
            EnergyLevel required = requiredEnergy(temporal);
            int needMinutes = (int) (t.getEstimatedDuration() == null ? 30 : t.getEstimatedDuration().toMinutes());

            double score = baseScore(t);
            score += energyMatch(available, required);

            String reason;
            // Temporal-fit adjustments — each sets the human explanation.
            if (temporal == TaskTemporalType.INDIVISIBLE && needMinutes > availableMinutes) {
                score -= 1000; // never split an indivisible task into a short gap
                reason = "Needs " + needMinutes + " uninterrupted min — wait for a longer block.";
            } else if (temporal == TaskTemporalType.FIXED_TIME && dueToday(t, now.toLocalDate())) {
                score += 50;
                reason = "Fixed-time task scheduled for today — handle it on time.";
            } else if (temporal == TaskTemporalType.DEEP_WORK || temporal == TaskTemporalType.INDIVISIBLE) {
                boolean prime = profile != null && period == profile.getBestDeepWorkPeriod() && available == EnergyLevel.HIGH;
                if (prime) { score += 25; reason = "High-energy " + period.label().toLowerCase()
                        + " window — ideal for deep, hard work."; }
                else if (available == EnergyLevel.LOW) { score -= 15;
                    reason = "Hard task, but energy is low right now — consider a lighter one."; }
                else reason = "Good focus window for cognitively demanding work.";
            } else { // FLEXIBLE / FIXED_TIME not due today
                if (available == EnergyLevel.LOW) { score += 10;
                    reason = "Light, flexible task — a good use of lower-energy time."; }
                else reason = "Flexible task you can clear whenever it fits.";
            }

            if (score > bestScore) {
                bestScore = score;
                best = t;
                bestReason = reason;
            }
        }

        if (best == null) return Optional.empty();
        double confidence = Math.max(0.05, Math.min(0.99, bestScore / 130.0));
        return Optional.of(new Recommendation(best, bestReason, confidence));
    }

    /* ---------------- helpers ---------------- */

    private static EnergyLevel requiredEnergy(TaskTemporalType t) {
        return switch (t) {
            case DEEP_WORK, INDIVISIBLE -> EnergyLevel.HIGH;
            case FIXED_TIME -> EnergyLevel.MEDIUM;
            case FLEXIBLE -> EnergyLevel.LOW;
        };
    }

    /** Priority + deadline + goal contribution, the time-agnostic part of the score. */
    private static double baseScore(Task t) {
        double priority = switch (t.getPriority() == null ? model.Priority.MEDIUM : t.getPriority()) {
            case HIGH -> 25; case MEDIUM -> 15; case LOW -> 5;
        };
        double urgency = 4;
        if (t.getDeadline() != null) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), t.getDeadline());
            urgency = days <= 0 ? 25 : days <= 1 ? 18 : days <= 3 ? 10 : 4;
        }
        return priority + urgency + t.getGoalContribution() * 15;
    }

    /** Reward when current energy meets or exceeds what the task needs. */
    private static double energyMatch(EnergyLevel available, EnergyLevel required) {
        int diff = available.weight() - required.weight();
        if (diff >= 0) return 30;
        if (diff == -1) return 12;
        return 2;
    }

    private static boolean dueToday(Task t, LocalDate today) {
        return t.getDeadline() != null && !t.getDeadline().isAfter(today);
    }

    private static TimeBlock currentBlock(List<TimeBlock> blocks, LocalTime now) {
        if (blocks == null) return null;
        for (TimeBlock b : blocks) if (b.contains(now)) return b;
        return null;
    }
}

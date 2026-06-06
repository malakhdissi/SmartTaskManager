package strategy;

import model.Recommendation;
import model.Task;
import model.TaskStatus;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * DefaultRecommendationStrategy — picks by score and explains the reason.
 *
 * <p>Reasoning rules (in order):
 * <ol>
 *   <li>If the task is due today or overdue → mention deadline pressure.</li>
 *   <li>Else if goal contribution ≥ 0.6 → mention goal alignment.</li>
 *   <li>Else if it is a DEEP_WORK task → mention focus opportunity.</li>
 *   <li>Else generic priority-based reasoning.</li>
 * </ol>
 * </p>
 */
public class DefaultRecommendationStrategy implements RecommendationStrategy {

    @Override
    public Recommendation pickBestNext(List<Task> candidates) {
        List<Recommendation> all = topN(candidates, 1);
        return all.isEmpty() ? null : all.get(0);
    }

    @Override
    public List<Recommendation> topN(List<Task> candidates, int n) {
        List<Recommendation> result = new ArrayList<>();
        candidates.stream()
                .filter(t -> t.getStatus() != TaskStatus.DONE && t.getStatus() != TaskStatus.SKIPPED)
                .sorted(Comparator.comparingDouble(Task::getScore).reversed())
                .limit(n)
                .forEach(t -> result.add(new Recommendation(t, explain(t), confidence(t))));
        return result;
    }

    private String explain(Task t) {
        if (t.getDeadline() != null) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), t.getDeadline());
            if (days <= 0) return "Due today — best to clear this first.";
            if (days <= 1) return "Deadline tomorrow + high goal contribution.";
        }
        if (t.getGoalContribution() >= 0.6) {
            return "Strong contribution (" + Math.round(t.getGoalContribution() * 100) + "%) to your active goal.";
        }
        if (t.getType() != null && t.getType().name().equals("DEEP_WORK")) {
            return "Deep Work block — best done now while focus is high.";
        }
        return "Top-ranked by priority and momentum.";
    }

    private double confidence(Task t) {
        // Map score 0..100 to a 0..1 confidence; clamp.
        return Math.max(0.05, Math.min(0.99, t.getScore() / 100d));
    }
}

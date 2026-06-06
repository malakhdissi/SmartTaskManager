package strategy;

import model.Priority;
import model.Task;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * DefaultScoringStrategy — simple, transparent heuristic.
 *
 * <p>score = priority weight + urgency (days-to-deadline) + goal contribution.
 * Easy to explain to a beginner, easy to test, easy to swap later for an
 * ML-driven implementation behind the same interface.</p>
 */
public class DefaultScoringStrategy implements ScoringStrategy {

    @Override
    public double score(Task task) {
        double priorityWeight = switch (task.getPriority() == null ? Priority.MEDIUM : task.getPriority()) {
            case HIGH -> 40;
            case MEDIUM -> 25;
            case LOW -> 10;
        };

        double urgency = 0;
        if (task.getDeadline() != null) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), task.getDeadline());
            // The closer the deadline, the higher the urgency contribution (capped).
            if (days <= 0) urgency = 30;
            else if (days <= 1) urgency = 25;
            else if (days <= 3) urgency = 18;
            else if (days <= 7) urgency = 10;
            else urgency = 4;
        }

        double goal = task.getGoalContribution() * 30; // up to 30 points
        double total = priorityWeight + urgency + goal;
        return Math.max(0, Math.min(100, total));
    }
}

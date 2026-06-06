package strategy;

import model.CoachContext;
import model.Task;

/**
 * Nudge against procrastination. Full behavioral history isn't tracked yet
 * (Phases 4/7), so this degrades honestly — it only uses real signals it has
 * (overdue status, very short "starter" tasks).
 */
public class ProcrastinationLens implements CoachLens {

    @Override public String name() { return "Momentum"; }
    @Override public double weight() { return 0.03; }

    @Override
    public LensResult evaluate(Task task, CoachContext ctx) {
        if (task.getDeadline() != null && task.getDeadline().isBefore(ctx.now().toLocalDate())) {
            return new LensResult(0.7, "Clearing overdue work breaks the stall.", true, 0.6);
        }
        int est = task.getEstimatedDuration() == null ? 30 : (int) task.getEstimatedDuration().toMinutes();
        if (est <= 15) {
            return new LensResult(0.6, "A quick win to build momentum.", true, 0.5);
        }
        return new LensResult(0.5, "Limited behavioral history yet.", false, 0.3);
    }
}

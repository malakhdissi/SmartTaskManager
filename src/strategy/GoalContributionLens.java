package strategy;

import model.CoachContext;
import model.Task;

/** How much the task advances the active goal. Degrades honestly with no goal link. */
public class GoalContributionLens implements CoachLens {

    @Override public String name() { return "Goal"; }
    @Override public double weight() { return 0.2; }

    @Override
    public LensResult evaluate(Task task, CoachContext ctx) {
        if (!ctx.goalsAvailable()) {
            return new LensResult(0.4, "No active goal yet — link tasks to a goal to improve this.", false, 0.3);
        }
        boolean linkedToActive = ctx.activeGoalId() != null && ctx.activeGoalId().equals(task.getGoalId());
        double c = task.getGoalContribution();
        if (!linkedToActive || c <= 0) {
            return new LensResult(0.4, "Not linked to your active goal.", false, 0.4);
        }
        int pct = (int) Math.round(c * 100);
        String goal = ctx.activeGoalTitle() == null ? "your active goal" : "\"" + ctx.activeGoalTitle() + "\"";
        return new LensResult(Math.min(1.0, c), "Contributes " + pct + "% to " + goal + ".", true, 0.85);
    }
}

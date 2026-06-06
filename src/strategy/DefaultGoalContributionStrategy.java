package strategy;

import model.Goal;
import model.Task;
import model.TaskStatus;

import java.util.List;

/**
 * DefaultGoalContributionStrategy — linear model: a task contributes its
 * (0..1) weight to the goal it is linked to, and goal progress is the sum of
 * contributions from <em>completed</em> linked tasks, capped at 100%.
 */
public class DefaultGoalContributionStrategy implements GoalContributionStrategy {

    @Override
    public double contribution(Task task, Goal goal) {
        if (task == null || goal == null || task.getGoalId() == null) return 0;
        if (!task.getGoalId().equals(goal.getId())) return 0;
        return Math.max(0, Math.min(1, task.getGoalContribution()));
    }

    @Override
    public int progressPercent(Goal goal, List<Task> goalTasks) {
        if (goal == null || goalTasks == null) return 0;
        double done = 0;
        for (Task t : goalTasks) {
            if (t.getStatus() == TaskStatus.DONE) done += contribution(t, goal);
        }
        return (int) Math.min(100, Math.round(done * 100));
    }
}

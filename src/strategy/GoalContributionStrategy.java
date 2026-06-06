package strategy;

import model.Goal;
import model.Task;

import java.util.List;

/**
 * GoalContributionStrategy — how a task's contribution to a goal is interpreted
 * and how goal progress is computed from its linked tasks. Behind an interface
 * so the formula (linear sum today, weighted/decayed later) can evolve.
 */
public interface GoalContributionStrategy {

    /** Contribution (0..1) of a task toward the goal; 0 if the task isn't linked to it. */
    double contribution(Task task, Goal goal);

    /** Goal progress as a 0..100 percent, computed from completed linked tasks. */
    int progressPercent(Goal goal, List<Task> goalTasks);
}

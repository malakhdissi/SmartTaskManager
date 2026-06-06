package service;

import model.Goal;
import model.Task;
import model.TaskStatus;
import strategy.GoalContributionStrategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * GoalProgressService — computes goal progress and contribution analytics from
 * the user's <em>real</em> linked tasks. No stored/fake progress: everything is
 * derived live from task status + contribution via {@link GoalContributionStrategy}.
 * Never throws.
 */
public class GoalProgressService {

    private final TaskService tasks;
    private final GoalContributionStrategy strategy;

    public GoalProgressService(TaskService tasks, GoalContributionStrategy strategy) {
        this.tasks = tasks;
        this.strategy = strategy;
    }

    /** Tasks linked to the given goal id. */
    public List<Task> linkedTasks(String goalId) {
        List<Task> out = new ArrayList<>();
        if (goalId == null) return out;
        for (Task t : safeAll()) {
            if (goalId.equals(t.getGoalId())) out.add(t);
        }
        return out;
    }

    /** Computed progress (0..100) of a goal from its completed linked tasks. */
    public int progressPercent(Goal goal) {
        if (goal == null) return 0;
        return strategy.progressPercent(goal, linkedTasks(goal.getId()));
    }

    /** How many tasks are linked to the goal, and how many are done. */
    public int linkedCount(Goal goal) { return goal == null ? 0 : linkedTasks(goal.getId()).size(); }

    public int completedCount(Goal goal) {
        if (goal == null) return 0;
        return (int) linkedTasks(goal.getId()).stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
    }

    /** Highest-contribution linked tasks (for "top contributors"). */
    public List<Task> topContributors(Goal goal, int n) {
        if (goal == null) return List.of();
        List<Task> linked = linkedTasks(goal.getId());
        linked.sort(Comparator.comparingDouble(Task::getGoalContribution).reversed());
        return linked.subList(0, Math.min(n, linked.size()));
    }

    private List<Task> safeAll() {
        try { return tasks.getAllTasks(); } catch (Exception e) { return List.of(); }
    }
}

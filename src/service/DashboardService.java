package service;

import model.Goal;
import model.Kpi;
import model.Task;
import model.TaskStatus;
import model.TaskType;

import java.util.List;

/**
 * DashboardService — supplies KPI cards + greeting context to the dashboard.
 *
 * <p>Honest by construction: every metric is derived from the user's real
 * tasks and active goal via {@link TaskService}/{@link GoalService}. A brand-new
 * account sees genuine zeros — never fabricated or sample data.</p>
 *
 * <p>Resilient by construction: this service NEVER throws. If the task/goal
 * data layer fails (e.g. a transient DB error), every accessor logs the cause
 * and returns a safe zero-state so the Dashboard always renders.</p>
 */
public class DashboardService {

    private final TaskService tasks;
    private final GoalService goals;
    private final GoalProgressService goalProgress;

    public DashboardService(TaskService tasks, GoalService goals) {
        this.tasks = tasks;
        this.goals = goals;
        this.goalProgress = new GoalProgressService(tasks, new strategy.DefaultGoalContributionStrategy());
    }

    /**
     * The four headline KPIs. All derived from real data; all naturally zero
     * for a new user. Guaranteed non-null and non-throwing.
     */
    public List<Kpi> getKpis() {
        try {
            List<Task> all = tasks.getAllTasks();
            long total = all.size();
            long done = all.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();

            int focusScore = total == 0 ? 0 : (int) Math.round(100.0 * done / total);

            long deepWorkMinutes = all.stream()
                    .filter(t -> t.getStatus() == TaskStatus.DONE && t.getType() == TaskType.DEEP_WORK)
                    .mapToLong(t -> t.getEstimatedDuration() == null ? 0 : t.getEstimatedDuration().toMinutes())
                    .sum();

            Goal active = goals.getActive();
            int goalPct = active == null ? 0 : goalProgress.progressPercent(active); // computed from linked tasks

            return List.of(
                    new Kpi("Focus Score",     focusScore + "%",            null, "primary"),
                    new Kpi("Deep Work",       formatHours(deepWorkMinutes), null, "intel"),
                    new Kpi("Tasks Completed", String.valueOf(done),        null, "success"),
                    new Kpi("Goal Progress",   goalPct + "%",               null, "warning"));
        } catch (Exception e) {
            System.err.println("[DashboardService] getKpis failed, showing zero-state: " + e);
            e.printStackTrace();
            return zeroStateKpis();
        }
    }

    /** Safe zero-state used for a brand-new user and as the error fallback. */
    public static List<Kpi> zeroStateKpis() {
        return List.of(
                new Kpi("Focus Score",     "0%", null, "primary"),
                new Kpi("Deep Work",       "0h", null, "intel"),
                new Kpi("Tasks Completed", "0",  null, "success"),
                new Kpi("Goal Progress",   "0%", null, "warning"));
    }

    /** Quick human-readable productivity label for the top bar. Never throws. */
    public String getProductivityLevel() {
        try {
            List<Task> all = tasks.getAllTasks();
            if (all.isEmpty()) return "Getting started";
            long done = all.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
            double ratio = (double) done / all.size();
            if (ratio >= 0.7) return "On track";
            if (ratio >= 0.3) return "Building momentum";
            return "Getting started";
        } catch (Exception e) {
            System.err.println("[DashboardService] getProductivityLevel failed: " + e);
            return "Getting started";
        }
    }

    /** Formats minutes as a compact hours string: 0 → "0h", 90 → "1.5h", 120 → "2h". */
    private static String formatHours(long minutes) {
        if (minutes <= 0) return "0h";
        double hours = minutes / 60.0;
        if (hours == Math.floor(hours)) return (long) hours + "h";
        return String.format(java.util.Locale.US, "%.1fh", hours);
    }
}

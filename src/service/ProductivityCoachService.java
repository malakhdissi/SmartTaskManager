package service;

import model.AvailableTimeSlot;
import model.CoachAvoidItem;
import model.CoachContext;
import model.CoachInsight;
import model.CoachRecommendation;
import model.DayPeriod;
import model.EnergyLevel;
import model.Goal;
import model.Priority;
import model.Task;
import model.TaskStatus;
import model.TaskTemporalType;
import model.TemporalProfile;
import strategy.CompositeCoachStrategy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ProductivityCoachService — the Coach V2 brain. Builds a {@link CoachContext}
 * from real application data + the user's selected energy and available time,
 * then uses {@link CompositeCoachStrategy} to recommend, explain, list
 * alternatives, and flag what to avoid.
 *
 * <p>Honest by construction: never crashes when goals/deep-work data are
 * missing, never fabricates signals, and returns empty results when there are
 * no tasks. The user's energy/time selection is held here so the UI's no-arg
 * calls reflect the current choice.</p>
 */
public class ProductivityCoachService {

    private final TaskService tasks;
    private final GoalService goals;
    private final AnalyticsService analytics;
    private final TemporalPlanningService temporal;
    private final CompositeCoachStrategy strategy = new CompositeCoachStrategy();
    private final GoalProgressService goalProgress;

    private EnergyLevel selectedEnergy;                 // null → derive from profile
    private AvailableTimeSlot selectedSlot = AvailableTimeSlot.HOUR;

    public ProductivityCoachService(TaskService tasks, GoalService goals,
                                    AnalyticsService analytics, TemporalPlanningService temporal) {
        this.tasks = tasks;
        this.goals = goals;
        this.analytics = analytics;
        this.temporal = temporal;
        this.goalProgress = new GoalProgressService(tasks, new strategy.DefaultGoalContributionStrategy());
    }

    /* ---------------- user selection ---------------- */

    public void setEnergy(EnergyLevel energy) { this.selectedEnergy = energy; }
    public void setAvailableTime(AvailableTimeSlot slot) { if (slot != null) this.selectedSlot = slot; }
    public EnergyLevel getEnergy() { return selectedEnergy != null ? selectedEnergy : defaultEnergy(); }
    public AvailableTimeSlot getAvailableTime() { return selectedSlot; }

    /* ---------------- situation ---------------- */

    public CoachContext analyzeCurrentSituation() {
        List<Task> all = safeAllTasks();
        List<Task> active = active(all);
        LocalDate today = LocalDate.now();

        int overdue = (int) active.stream()
                .filter(t -> t.getDeadline() != null && t.getDeadline().isBefore(today)).count();
        int completed = (int) all.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();

        Goal goal = safeActiveGoal();
        boolean goalsAvailable = goal != null;
        String goalId = goal == null ? null : goal.getId();
        String goalTitle = goal == null ? null : goal.getTitle();
        // Progress is COMPUTED from completed linked tasks, never the stored value.
        Integer goalProgressPct = goal == null ? null : goalProgress.progressPercent(goal);

        Long deepWorkMinutes = null;
        try { deepWorkMinutes = analytics.snapshot().deepWorkMinutes(); } catch (Exception ignored) { }
        // Real per-session deep-work history is not persisted yet (Phase 5).
        boolean deepWorkAvailable = false;

        DayPeriod period = DayPeriod.of(LocalDateTime.now().toLocalTime());
        DayPeriod bestDeep = safeBestDeepWorkPeriod();

        return new CoachContext(LocalDateTime.now(), period, getEnergy(), selectedSlot.minutes(),
                active, overdue, completed, goalId, goalTitle, goalProgressPct, deepWorkMinutes, bestDeep,
                goalsAvailable, deepWorkAvailable, !active.isEmpty());
    }

    /* ---------------- recommendations ---------------- */

    public CoachRecommendation recommendNextAction() {
        List<CoachRecommendation> ranked = ranked();
        return ranked.isEmpty() ? null : ranked.get(0);
    }

    public CoachRecommendation recommendForAvailableTime(AvailableTimeSlot slot) {
        setAvailableTime(slot);
        return recommendNextAction();
    }

    public CoachRecommendation recommendForEnergyLevel(EnergyLevel level) {
        setEnergy(level);
        return recommendNextAction();
    }

    /** Up to 3 alternatives (the next-best tasks after the recommended one). */
    public List<CoachRecommendation> generateAlternatives() {
        List<CoachRecommendation> ranked = ranked();
        if (ranked.size() <= 1) return List.of();
        return ranked.subList(1, Math.min(ranked.size(), 4));
    }

    /** Tasks to defer right now, with honest reasons. Excludes the recommended task. */
    public List<CoachAvoidItem> generateAvoidList() {
        CoachContext ctx = analyzeCurrentSituation();
        CoachRecommendation best = recommendNextAction();
        String bestId = best == null ? null : best.task().getId();
        List<CoachAvoidItem> out = new ArrayList<>();
        for (Task t : ctx.activeTasks()) {
            if (t.getId().equals(bestId)) continue;
            String reason = avoidReason(t, ctx);
            if (reason != null) out.add(new CoachAvoidItem(t.getTitle(), reason));
            if (out.size() >= 4) break;
        }
        return out;
    }

    /* ---------------- insights ---------------- */

    public CoachInsight detectProcrastinationRisk() {
        CoachContext ctx = analyzeCurrentSituation();
        if (ctx.overdue() > 0) {
            return new CoachInsight(CoachInsight.Kind.PROCRASTINATION_RISK,
                    ctx.overdue() + " overdue task(s) — risk of stalling. Start the smallest one.", true);
        }
        return null;
    }

    public CoachInsight detectFocusRisk() {
        CoachContext ctx = analyzeCurrentSituation();
        boolean hardPending = ctx.activeTasks().stream().anyMatch(this::isDeep);
        if (hardPending && ctx.energy() == EnergyLevel.LOW) {
            return new CoachInsight(CoachInsight.Kind.FOCUS_RISK,
                    "Demanding tasks are pending but your energy is low — protect a real focus block or pick a lighter task.", true);
        }
        return null;
    }

    public List<CoachInsight> generateInsights() {
        CoachContext ctx = analyzeCurrentSituation();
        List<CoachInsight> out = new ArrayList<>();
        CoachInsight pro = detectProcrastinationRisk();
        if (pro != null) out.add(pro);
        CoachInsight focus = detectFocusRisk();
        if (focus != null) out.add(focus);

        long dueSoon = ctx.activeTasks().stream()
                .filter(t -> t.getDeadline() != null
                        && !t.getDeadline().isAfter(ctx.now().toLocalDate().plusDays(2))).count();
        if (dueSoon > 0) {
            out.add(new CoachInsight(CoachInsight.Kind.DEADLINE_PRESSURE,
                    dueSoon + " task(s) due within 2 days.", true));
        }
        if (ctx.activeTasks().size() > 8) {
            out.add(new CoachInsight(CoachInsight.Kind.OVERLOAD,
                    "You have " + ctx.activeTasks().size() + " active tasks — consider deferring low-impact ones.", true));
        }
        CoachRecommendation best = recommendNextAction();
        if (best != null) {
            out.add(new CoachInsight(CoachInsight.Kind.CONFIDENCE,
                    "Recommendation confidence: " + (int) Math.round(best.confidence() * 100) + "%.", true));
        }
        if (!ctx.goalsAvailable()) {
            out.add(new CoachInsight(CoachInsight.Kind.INFO,
                    "Goal contribution unavailable — no active goal linked yet.", false));
        }
        return out;
    }

    /* ---------------- internals ---------------- */

    private List<CoachRecommendation> ranked() {
        CoachContext ctx = analyzeCurrentSituation();
        if (!ctx.hasTasks()) return List.of();
        return strategy.rank(ctx.activeTasks(), ctx);
    }

    private String avoidReason(Task t, CoachContext ctx) {
        int est = t.getEstimatedDuration() == null ? 30 : (int) t.getEstimatedDuration().toMinutes();
        if (est > ctx.availableMinutes()) {
            return "Needs " + est + " min — longer than your " + ctx.availableMinutes() + "-minute window.";
        }
        if (requiredEnergy(t.getTemporalType()).weight() - ctx.energy().weight() >= 2) {
            return "Too demanding for your " + ctx.energy().label().toLowerCase() + " energy right now.";
        }
        boolean noDeadlinePressure = t.getDeadline() == null
                || t.getDeadline().isAfter(ctx.now().toLocalDate().plusDays(7));
        if (t.getPriority() == Priority.LOW && noDeadlinePressure && t.getGoalContribution() <= 0) {
            return "Low priority, no deadline pressure, and not linked to a goal — low impact now.";
        }
        return null;
    }

    private boolean isDeep(Task t) {
        TaskTemporalType tt = t.getTemporalType();
        return tt == TaskTemporalType.DEEP_WORK || tt == TaskTemporalType.INDIVISIBLE;
    }

    private static EnergyLevel requiredEnergy(TaskTemporalType t) {
        return switch (t) {
            case DEEP_WORK, INDIVISIBLE -> EnergyLevel.HIGH;
            case FIXED_TIME -> EnergyLevel.MEDIUM;
            case FLEXIBLE -> EnergyLevel.LOW;
        };
    }

    private List<Task> active(List<Task> all) {
        List<Task> out = new ArrayList<>();
        for (Task t : all) if (t.getStatus() != TaskStatus.DONE && t.getStatus() != TaskStatus.SKIPPED) out.add(t);
        return out;
    }

    private List<Task> safeAllTasks() {
        try { return tasks.getAllTasks(); } catch (Exception e) { return List.of(); }
    }

    private Goal safeActiveGoal() {
        try { return goals.getActive(); } catch (Exception e) { return null; }
    }

    private EnergyLevel defaultEnergy() {
        try {
            return temporal.getProfile().energyFor(DayPeriod.of(LocalDateTime.now().toLocalTime()));
        } catch (Exception e) {
            return EnergyLevel.MEDIUM;
        }
    }

    private DayPeriod safeBestDeepWorkPeriod() {
        try { return temporal.getProfile().getBestDeepWorkPeriod(); } catch (Exception e) { return DayPeriod.MORNING; }
    }
}

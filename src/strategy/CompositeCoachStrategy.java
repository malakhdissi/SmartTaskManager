package strategy;

import model.CoachContext;
import model.CoachReason;
import model.CoachRecommendation;
import model.Task;
import util.Formatter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * CompositeCoachStrategy — runs every {@link CoachLens} over each task, blends
 * the scores by weight, and produces ranked {@link CoachRecommendation}s with a
 * full, explainable reason breakdown. Pure and deterministic — no randomness,
 * no fabricated data.
 */
public class CompositeCoachStrategy {

    private final List<CoachLens> lenses;

    public CompositeCoachStrategy() {
        this.lenses = List.of(
                new DeadlineLens(), new PriorityLens(), new GoalContributionLens(),
                new DurationFitLens(), new EnergyAwareLens(), new TemporalLens(),
                new ProcrastinationLens(), new DeepWorkLens());
    }

    /** Ranks the given tasks (highest fit first) within the context. */
    public List<CoachRecommendation> rank(List<Task> tasks, CoachContext ctx) {
        List<CoachRecommendation> out = new ArrayList<>();
        for (Task t : tasks) {
            double weighted = 0, weightSum = 0, confWeighted = 0;
            List<CoachReason> reasons = new ArrayList<>();
            for (CoachLens lens : lenses) {
                CoachLens.LensResult r = lens.evaluate(t, ctx);
                weighted += r.score() * lens.weight();
                confWeighted += r.confidence() * lens.weight();
                weightSum += lens.weight();
                reasons.add(new CoachReason(lens.name(), r.score(), r.reason(), r.realData(), r.confidence()));
            }
            double score = weightSum > 0 ? weighted / weightSum : 0;
            double confidence = weightSum > 0 ? confWeighted / weightSum : 0;
            out.add(new CoachRecommendation(t, score, confidence, reasons,
                    goalLabel(t, ctx), durationLabel(t), priorityLabel(t), deadlineLabel(t)));
        }
        out.sort(Comparator.comparingDouble(CoachRecommendation::score).reversed());
        return out;
    }

    /* ---------------- display label helpers ---------------- */

    private static String goalLabel(Task t, CoachContext ctx) {
        if (!ctx.goalsAvailable() || t.getGoalContribution() <= 0) return "Not linked to a goal yet";
        return Math.round(t.getGoalContribution() * 100) + "% to your goal";
    }

    private static String durationLabel(Task t) {
        return t.getEstimatedDuration() == null ? "—" : Formatter.duration(t.getEstimatedDuration());
    }

    private static String priorityLabel(Task t) {
        return t.getPriority() == null ? "—" : t.getPriority().label();
    }

    private static String deadlineLabel(Task t) {
        return t.getDeadline() == null ? "No deadline" : "Due " + Formatter.date(t.getDeadline());
    }
}

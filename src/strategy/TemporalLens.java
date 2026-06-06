package strategy;

import model.CoachContext;
import model.Task;
import model.TaskTemporalType;

/** Fits deep-work tasks to the user's best deep-work period. */
public class TemporalLens implements CoachLens {

    @Override public String name() { return "Temporal"; }
    @Override public double weight() { return 0.05; }

    @Override
    public LensResult evaluate(Task task, CoachContext ctx) {
        TaskTemporalType tt = task.getTemporalType();
        boolean deep = tt == TaskTemporalType.DEEP_WORK || tt == TaskTemporalType.INDIVISIBLE;
        if (deep && ctx.bestDeepWorkPeriod() == ctx.period()) {
            return new LensResult(0.85, "Now (" + ctx.period().label().toLowerCase()
                    + ") is your best deep-work window.", true, 0.7);
        }
        if (deep) {
            return new LensResult(0.4, "Deep work lands better in your "
                    + ctx.bestDeepWorkPeriod().label().toLowerCase() + " window.", true, 0.6);
        }
        return new LensResult(0.6, "Can be done in any part of the day.", true, 0.5);
    }
}

package strategy;

import model.CoachContext;
import model.Task;
import model.TaskTemporalType;

/** Whether the task fits the user's selected available time. */
public class DurationFitLens implements CoachLens {

    @Override public String name() { return "Time fit"; }
    @Override public double weight() { return 0.15; }

    @Override
    public LensResult evaluate(Task task, CoachContext ctx) {
        int est = task.getEstimatedDuration() == null ? 30 : (int) task.getEstimatedDuration().toMinutes();
        int avail = ctx.availableMinutes();
        if (est <= avail) {
            return new LensResult(0.9, "Fits your " + avail + "-minute window (" + est + " min).", true, 0.9);
        }
        if (task.getTemporalType() == TaskTemporalType.INDIVISIBLE) {
            return new LensResult(0.0, "Needs " + est + " uninterrupted min — longer than your "
                    + avail + "-minute window; don't start it now.", true, 0.9);
        }
        return new LensResult(0.15, "Needs " + est + " min — longer than your " + avail + "-minute window.", true, 0.85);
    }
}

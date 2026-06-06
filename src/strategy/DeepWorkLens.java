package strategy;

import model.CoachContext;
import model.EnergyLevel;
import model.Task;
import model.TaskTemporalType;

/**
 * Favors deep-work tasks when conditions are good. Real focus history isn't
 * tracked yet (Phase 5), so without it this stays neutral and says so.
 */
public class DeepWorkLens implements CoachLens {

    @Override public String name() { return "Deep work"; }
    @Override public double weight() { return 0.02; }

    @Override
    public LensResult evaluate(Task task, CoachContext ctx) {
        if (!ctx.deepWorkAvailable()) {
            return new LensResult(0.5, "No deep-work history yet to personalize this.", false, 0.3);
        }
        TaskTemporalType tt = task.getTemporalType();
        boolean deep = tt == TaskTemporalType.DEEP_WORK || tt == TaskTemporalType.INDIVISIBLE;
        if (deep && ctx.energy() == EnergyLevel.HIGH && ctx.availableMinutes() >= 50) {
            return new LensResult(0.8, "Good conditions for deep work right now.", true, 0.6);
        }
        return new LensResult(0.5, "Standard focus conditions.", true, 0.4);
    }
}

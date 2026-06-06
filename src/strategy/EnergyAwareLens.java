package strategy;

import model.CoachContext;
import model.EnergyLevel;
import model.Task;
import model.TaskTemporalType;

/** Matches task difficulty (from temporal type) to the user's selected energy. */
public class EnergyAwareLens implements CoachLens {

    @Override public String name() { return "Energy"; }
    @Override public double weight() { return 0.1; }

    @Override
    public LensResult evaluate(Task task, CoachContext ctx) {
        EnergyLevel required = requiredEnergy(task.getTemporalType());
        EnergyLevel have = ctx.energy();
        int diff = have.weight() - required.weight();
        String lvl = have.label().toLowerCase();
        if (diff >= 1)  return new LensResult(0.6,  "You have more energy than this needs — fine, or save it for harder work.", true, 0.6);
        if (diff == 0)  return new LensResult(0.85, "Suits your " + lvl + " energy.", true, 0.8);
        if (diff == -1) return new LensResult(0.35, "A bit demanding for your " + lvl + " energy.", true, 0.7);
        return new LensResult(0.15, "Too demanding for your " + lvl + " energy right now.", true, 0.7);
    }

    static EnergyLevel requiredEnergy(TaskTemporalType t) {
        return switch (t) {
            case DEEP_WORK, INDIVISIBLE -> EnergyLevel.HIGH;
            case FIXED_TIME -> EnergyLevel.MEDIUM;
            case FLEXIBLE -> EnergyLevel.LOW;
        };
    }
}

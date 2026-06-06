package strategy;

import model.CoachContext;
import model.Priority;
import model.Task;

/** Importance from the task's priority. */
public class PriorityLens implements CoachLens {

    @Override public String name() { return "Priority"; }
    @Override public double weight() { return 0.2; }

    @Override
    public LensResult evaluate(Task task, CoachContext ctx) {
        Priority p = task.getPriority() == null ? Priority.MEDIUM : task.getPriority();
        return switch (p) {
            case HIGH   -> new LensResult(0.9,  "High priority.", true, 0.9);
            case MEDIUM -> new LensResult(0.55, "Medium priority.", true, 0.8);
            case LOW    -> new LensResult(0.3,  "Low priority.", true, 0.8);
        };
    }
}

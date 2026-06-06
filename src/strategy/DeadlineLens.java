package strategy;

import model.CoachContext;
import model.Task;

import java.time.temporal.ChronoUnit;

/** Urgency from the deadline. Degrades honestly when no deadline is set. */
public class DeadlineLens implements CoachLens {

    @Override public String name() { return "Deadline"; }
    @Override public double weight() { return 0.25; }

    @Override
    public LensResult evaluate(Task task, CoachContext ctx) {
        if (task.getDeadline() == null) {
            return new LensResult(0.4, "No deadline set — no urgency pressure.", false, 0.3);
        }
        long days = ChronoUnit.DAYS.between(ctx.now().toLocalDate(), task.getDeadline());
        if (days < 0)  return new LensResult(1.0,  "Overdue by " + (-days) + " day(s) — clear it first.", true, 0.95);
        if (days == 0) return new LensResult(0.95, "Due today — high urgency.", true, 0.95);
        if (days == 1) return new LensResult(0.85, "Due tomorrow — urgency is high.", true, 0.9);
        if (days <= 3) return new LensResult(0.6,  "Due in " + days + " days.", true, 0.8);
        if (days <= 7) return new LensResult(0.4,  "Due within a week.", true, 0.7);
        return new LensResult(0.25, "Deadline is more than a week away.", true, 0.6);
    }
}

package strategy;

import model.CoachContext;
import model.Task;

/**
 * CoachLens — one scoring perspective on a task within a {@link CoachContext}.
 *
 * <p>Each lens returns a normalized score (0..1), a human-readable reason,
 * whether the signal is backed by real data, and a confidence. When data is
 * missing a lens must return a neutral score with an honest explanation
 * ({@code realData=false}) — never invent a signal.</p>
 */
public interface CoachLens {

    String name();

    /** Relative weight in the composite (weights need not sum to 1). */
    double weight();

    LensResult evaluate(Task task, CoachContext ctx);

    record LensResult(double score, String reason, boolean realData, double confidence) {
        public static LensResult neutral(String reason) {
            return new LensResult(0.4, reason, false, 0.3);
        }
    }
}

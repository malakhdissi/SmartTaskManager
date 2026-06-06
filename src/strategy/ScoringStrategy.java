package strategy;

import model.Task;

/**
 * ScoringStrategy — pluggable algorithm for ranking a Task's importance.
 *
 * <p>Why an interface: we expect to replace the scoring logic over time
 * (heuristic now → behavioral model later → AI model). Views never
 * recompute scores themselves; they read {@code task.getScore()} which is
 * filled by a service that delegates to a ScoringStrategy implementation.</p>
 */
public interface ScoringStrategy {
    /** Compute a 0..100 score for the given task. Higher = more important now. */
    double score(Task task);
}

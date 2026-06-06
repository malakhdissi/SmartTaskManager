package strategy;

import model.Recommendation;
import model.Task;

import java.util.List;

/**
 * RecommendationStrategy — picks the "best next action" + reasoning.
 *
 * <p>The interface always returns a {@link Recommendation} (task + reason)
 * rather than just a Task — the explanation is required by our UX principles.</p>
 */
public interface RecommendationStrategy {

    /** Returns the single best next action, or null if no candidates. */
    Recommendation pickBestNext(List<Task> candidates);

    /** Returns the top N candidates with their reasoning. */
    List<Recommendation> topN(List<Task> candidates, int n);
}

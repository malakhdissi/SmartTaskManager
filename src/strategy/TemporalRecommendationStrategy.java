package strategy;

import model.Recommendation;
import model.TemporalProfile;
import model.TimeBlock;
import model.Task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * TemporalRecommendationStrategy — chooses the best task to do <em>right now</em>
 * given the user's energy profile and the available time blocks.
 *
 * <p>Behind this interface so the matching algorithm can evolve (heuristic →
 * learned) without touching the service or UI.</p>
 */
public interface TemporalRecommendationStrategy {

    Optional<Recommendation> recommendNow(List<Task> tasks,
                                          TemporalProfile profile,
                                          List<TimeBlock> blocks,
                                          LocalDateTime now);
}

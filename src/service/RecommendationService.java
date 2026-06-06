package service;

import model.Recommendation;
import strategy.RecommendationStrategy;

import java.util.List;

/**
 * RecommendationService — thin facade over the recommendation strategy.
 *
 * <p>Views call {@code getBestNext()} / {@code getTopN(n)} without knowing
 * which algorithm is behind it. Future evolution: behavioral models, time
 * intelligence, AI ranking — all live behind {@link RecommendationStrategy}.</p>
 */
public class RecommendationService {

    private final TaskService taskService;
    private final RecommendationStrategy strategy;

    public RecommendationService(TaskService taskService, RecommendationStrategy strategy) {
        this.taskService = taskService;
        this.strategy = strategy;
    }

    public Recommendation getBestNext() {
        return strategy.pickBestNext(taskService.getAllTasks());
    }

    public List<Recommendation> getTopN(int n) {
        return strategy.topN(taskService.getAllTasks(), n);
    }
}

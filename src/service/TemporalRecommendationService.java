package service;

import model.Recommendation;
import model.Task;
import model.TaskStatus;
import model.TaskTemporalType;
import strategy.TemporalRecommendationStrategy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TemporalRecommendationService — answers "what should I do now?" using the
 * temporal strategy, and classifies the user's tasks by temporal type for the
 * Temporal Intelligence screen.
 */
public class TemporalRecommendationService {

    private final TaskService taskService;
    private final TemporalPlanningService planning;
    private final TemporalRecommendationStrategy strategy;

    public TemporalRecommendationService(TaskService taskService,
                                         TemporalPlanningService planning,
                                         TemporalRecommendationStrategy strategy) {
        this.taskService = taskService;
        this.planning = planning;
        this.strategy = strategy;
    }

    public Optional<Recommendation> bestTaskNow() {
        return bestTaskNow(LocalDateTime.now());
    }

    /** Testable overload with an explicit "now". */
    public Optional<Recommendation> bestTaskNow(LocalDateTime now) {
        return strategy.recommendNow(taskService.getAllTasks(), planning.getProfile(), planning.getBlocks(), now);
    }

    /** Active (not done/skipped) tasks whose effective temporal type matches. */
    public List<Task> tasksOfTemporalType(TaskTemporalType type) {
        return taskService.getAllTasks().stream()
                .filter(t -> t.getStatus() != TaskStatus.DONE && t.getStatus() != TaskStatus.SKIPPED)
                .filter(t -> t.getTemporalType() == type)
                .collect(Collectors.toList());
    }
}

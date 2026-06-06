package service;

import model.Priority;
import model.Task;
import model.TaskStatus;
import model.TaskType;
import model.TimelineEvent;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AnalyticsService — computes real productivity metrics from the user's tasks.
 *
 * <p>Honest by construction: every number is derived from {@link TaskService};
 * a user with no tasks gets genuine zeros and {@code hasData() == false} so the
 * UI can show a professional empty state instead of fake charts. Never throws.</p>
 */
public class AnalyticsService {

    private final TaskService tasks;

    public AnalyticsService(TaskService tasks) {
        this.tasks = tasks;
    }

    /** Immutable headline snapshot for the Insights screen. */
    public record Snapshot(long total, long completed, long active, long overdue,
                           int completionRate, long deepWorkMinutes, boolean hasData) {}

    public Snapshot snapshot() {
        List<Task> all = safeAll();
        LocalDate today = LocalDate.now();

        long total = all.size();
        long completed = all.stream().filter(t -> t.getStatus() == TaskStatus.DONE).count();
        long active = all.stream().filter(AnalyticsService::isActive).count();
        long overdue = all.stream()
                .filter(AnalyticsService::isActive)
                .filter(t -> t.getDeadline() != null && t.getDeadline().isBefore(today))
                .count();
        int completionRate = total == 0 ? 0 : (int) Math.round(100.0 * completed / total);
        long deepWorkMinutes = all.stream()
                .filter(t -> t.getStatus() == TaskStatus.DONE && t.getType() == TaskType.DEEP_WORK)
                .mapToLong(t -> t.getEstimatedDuration() == null ? 0 : t.getEstimatedDuration().toMinutes())
                .sum();

        return new Snapshot(total, completed, active, overdue, completionRate, deepWorkMinutes, total > 0);
    }

    /** Counts of active tasks by priority (insertion-ordered High→Low). */
    public Map<String, Integer> priorityDistribution() {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put("High", 0); m.put("Medium", 0); m.put("Low", 0);
        for (Task t : safeAll()) {
            if (!isActive(t) || t.getPriority() == null) continue;
            String key = switch (t.getPriority()) {
                case HIGH -> "High"; case MEDIUM -> "Medium"; case LOW -> "Low";
            };
            m.merge(key, 1, Integer::sum);
        }
        return m;
    }

    /** Counts of all tasks by status. */
    public Map<String, Integer> statusDistribution() {
        Map<String, Integer> m = new LinkedHashMap<>();
        m.put("To do", 0); m.put("In progress", 0); m.put("Done", 0); m.put("Skipped", 0);
        for (Task t : safeAll()) {
            if (t.getStatus() == null) continue;
            String key = switch (t.getStatus()) {
                case TODO -> "To do"; case IN_PROGRESS -> "In progress";
                case DONE -> "Done"; case SKIPPED -> "Skipped";
            };
            m.merge(key, 1, Integer::sum);
        }
        return m;
    }

    /**
     * A real activity timeline built from the user's completed tasks. Completion
     * timestamps aren't tracked yet, so {@code when} is null (rendered as "—")
     * rather than inventing a time. Empty when nothing is completed.
     */
    public List<TimelineEvent> recentTimeline() {
        List<TimelineEvent> out = new ArrayList<>();
        for (Task t : safeAll()) {
            if (t.getStatus() != TaskStatus.DONE) continue;
            String kind = t.getType() == null ? "task" : t.getType().label();
            out.add(new TimelineEvent(TimelineEvent.Kind.TASK_COMPLETED, t.getTitle(),
                    "Completed · " + kind, null));
        }
        return out;
    }

    private static boolean isActive(Task t) {
        return t.getStatus() == TaskStatus.TODO || t.getStatus() == TaskStatus.IN_PROGRESS;
    }

    private List<Task> safeAll() {
        try {
            return tasks.getAllTasks();
        } catch (Exception e) {
            System.err.println("[AnalyticsService] task fetch failed, treating as empty: " + e);
            return List.of();
        }
    }
}

package service;

import dao.InMemoryTaskDao;
import model.Priority;
import model.Task;
import model.TaskStatus;
import model.TaskType;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import strategy.DefaultScoringStrategy;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Analytics are real and honest: empty → hasData false + zeros; otherwise real counts. */
class AnalyticsServiceTest {

    private Session session;
    private TaskService tasks;
    private AnalyticsService analytics;

    @BeforeEach
    void setUp() {
        session = new Session();
        session.set(new User("u1", "Sam", "sam@example.com", null, 0, null));
        tasks = new TaskServiceImpl(new InMemoryTaskDao(session), new DefaultScoringStrategy());
        analytics = new AnalyticsService(tasks);
    }

    @Test
    void emptyDatabaseHasNoDataAndZeroMetrics() {
        AnalyticsService.Snapshot s = analytics.snapshot();
        assertFalse(s.hasData());
        assertEquals(0, s.total());
        assertEquals(0, s.completionRate());
    }

    @Test
    void metricsReflectRealTasks() {
        tasks.save(new Task("t1", "A", "", Priority.HIGH, TaskStatus.DONE,
                TaskType.DEEP_WORK, LocalDate.now(), Duration.ofMinutes(60), 0, 0.5));
        tasks.save(new Task("t2", "B", "", Priority.HIGH, TaskStatus.TODO,
                TaskType.ADMIN, LocalDate.now().minusDays(1), Duration.ofMinutes(30), 0, 0.1));

        AnalyticsService.Snapshot s = analytics.snapshot();
        assertTrue(s.hasData());
        assertEquals(2, s.total());
        assertEquals(1, s.completed());
        assertEquals(1, s.active());
        assertEquals(1, s.overdue(), "the active task with a past deadline is overdue");
        assertEquals(50, s.completionRate());
        assertEquals(60, s.deepWorkMinutes(), "completed deep-work minutes");
        assertEquals(1, (int) analytics.priorityDistribution().get("High"));
    }

    @Test
    void timelineIsEmptyWhenNothingIsCompleted() {
        tasks.save(new Task("t1", "Open", "", Priority.MEDIUM, TaskStatus.TODO,
                TaskType.OTHER, null, Duration.ofMinutes(10), 0, 0));
        assertTrue(analytics.recentTimeline().isEmpty());
    }

    @Test
    void timelineListsRealCompletedTasksWithNoFabricatedTime() {
        tasks.save(new Task("t1", "Finished report", "", Priority.HIGH, TaskStatus.DONE,
                TaskType.DEEP_WORK, LocalDate.now(), Duration.ofMinutes(60), 0, 0.5));
        var timeline = analytics.recentTimeline();
        assertEquals(1, timeline.size());
        assertEquals("Finished report", timeline.get(0).getTitle());
        assertNull(timeline.get(0).getWhen(), "no completion timestamp is invented");
    }
}

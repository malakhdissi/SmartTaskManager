package service;

import dao.GoalDao;
import dao.InMemoryGoalDao;
import dao.InMemoryTaskDao;
import dao.TaskDao;
import model.Kpi;
import model.Priority;
import model.Task;
import model.TaskStatus;
import model.TaskType;
import model.User;
import org.junit.jupiter.api.Test;
import strategy.DefaultScoringStrategy;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guarantees the Dashboard can always produce content without throwing, across
 * the scenarios the post-login navigation lands on:
 *   - new user with empty database
 *   - user with tasks
 *   - guest user
 *   - service / database error (fallback)
 */
class DashboardRenderTest {

    /** Wires a DashboardService over in-memory DAOs scoped to the given user id. */
    private DashboardService dashboardFor(String userId, String name) {
        Session session = new Session();
        session.set(new User(userId, name, name.toLowerCase() + "@example.com", null, 0, null));
        TaskDao taskDao = new InMemoryTaskDao(session);
        GoalDao goalDao = new InMemoryGoalDao(session);
        TaskService tasks = new TaskServiceImpl(taskDao, new DefaultScoringStrategy());
        return new DashboardService(tasks, new GoalService(goalDao));
    }

    @Test
    void newUserWithEmptyDatabaseShowsZeroState() {
        DashboardService dash = dashboardFor("u-new", "Newbie");

        List<Kpi> kpis = dash.getKpis();
        assertEquals(4, kpis.size());
        assertEquals("Focus Score", kpis.get(0).getLabel());
        assertEquals("0%", kpis.get(0).getValue());
        assertEquals("0h", kpis.get(1).getValue());   // Deep Work
        assertEquals("0",  kpis.get(2).getValue());   // Tasks Completed
        assertEquals("0%", kpis.get(3).getValue());   // Goal Progress
        assertEquals("Getting started", dash.getProductivityLevel());
    }

    @Test
    void userWithTasksReflectsRealNumbers() {
        Session session = new Session();
        session.set(new User("u-1", "Sam", "sam@example.com", null, 0, null));
        TaskDao taskDao = new InMemoryTaskDao(session);
        TaskService tasks = new TaskServiceImpl(taskDao, new DefaultScoringStrategy());
        DashboardService dash = new DashboardService(tasks, new GoalService(new InMemoryGoalDao(session)));

        tasks.save(new Task("t1", "Deep block", "", Priority.HIGH, TaskStatus.DONE,
                TaskType.DEEP_WORK, LocalDate.now(), Duration.ofMinutes(90), 0, 0.5));
        tasks.save(new Task("t2", "Todo item", "", Priority.LOW, TaskStatus.TODO,
                TaskType.ADMIN, null, Duration.ofMinutes(30), 0, 0.1));

        List<Kpi> kpis = dash.getKpis();
        assertEquals("50%",  kpis.get(0).getValue(), "1 of 2 tasks done");
        assertEquals("1.5h", kpis.get(1).getValue(), "90 min of completed deep work");
        assertEquals("1",    kpis.get(2).getValue(), "one completed task");
    }

    @Test
    void guestUserGetsEmptyDashboardNotCrash() {
        DashboardService dash = dashboardFor("guest-1", "Guest");
        List<Kpi> kpis = assertDoesNotThrow(dash::getKpis);
        assertEquals("0%", kpis.get(0).getValue());
        assertEquals("0",  kpis.get(2).getValue());
    }

    @Test
    void serviceErrorFallsBackToZeroStateWithoutThrowing() {
        // A TaskService whose every call fails — simulates a DB/service outage.
        TaskService boom = new TaskService() {
            @Override public List<Task> getAllTasks() { throw new RuntimeException("DB down"); }
            @Override public Optional<Task> getById(String id) { throw new RuntimeException("DB down"); }
            @Override public List<Task> getTopTasks(int n) { throw new RuntimeException("DB down"); }
            @Override public Task save(Task task) { throw new RuntimeException("DB down"); }
            @Override public boolean delete(String id) { throw new RuntimeException("DB down"); }
            @Override public Optional<Task> markDone(String id) { throw new RuntimeException("DB down"); }
        };
        Session session = new Session();
        DashboardService dash = new DashboardService(boom, new GoalService(new InMemoryGoalDao(session)));

        List<Kpi> kpis = assertDoesNotThrow(dash::getKpis);
        assertEquals(4, kpis.size(), "must still return the 4 KPI cards");
        assertEquals("0%", kpis.get(0).getValue());
        assertEquals("0h", kpis.get(1).getValue());
        assertEquals("0",  kpis.get(2).getValue());
        assertEquals("0%", kpis.get(3).getValue());
        assertEquals("Getting started", assertDoesNotThrow(dash::getProductivityLevel));
    }
}

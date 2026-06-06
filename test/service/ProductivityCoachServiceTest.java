package service;

import dao.InMemoryGoalDao;
import dao.InMemoryTaskDao;
import dao.InMemoryTemporalProfileDAO;
import dao.InMemoryTimeBlockDAO;
import model.AvailableTimeSlot;
import model.CoachAvoidItem;
import model.CoachReason;
import model.CoachRecommendation;
import model.EnergyLevel;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Coach V2 reasons over real data only: empty states, exclusions, urgency,
 * priority, time/energy adaptation, reasons/alternatives/avoid, honest goal degradation.
 */
class ProductivityCoachServiceTest {

    private Session session;
    private TaskService tasks;
    private ProductivityCoachService coach;

    @BeforeEach
    void setUp() {
        session = new Session();
        session.set(new User("u1", "Sam", "sam@example.com", null, 0, null));
        tasks = new TaskServiceImpl(new InMemoryTaskDao(session), new DefaultScoringStrategy());
        GoalService goals = new GoalService(new InMemoryGoalDao(session));
        AnalyticsService analytics = new AnalyticsService(tasks);
        TemporalPlanningService temporal = new TemporalPlanningService(
                new InMemoryTimeBlockDAO(session), new InMemoryTemporalProfileDAO(session), session);
        coach = new ProductivityCoachService(tasks, goals, analytics, temporal);
        coach.setEnergy(EnergyLevel.MEDIUM);                 // deterministic (not time-derived)
        coach.setAvailableTime(AvailableTimeSlot.HOUR);
    }

    private Task task(String id, Priority p, TaskStatus s, TaskType type, Integer deadlineInDays, int minutes, double goal) {
        LocalDate dl = deadlineInDays == null ? null : LocalDate.now().plusDays(deadlineInDays);
        return new Task(id, id, "", p, s, type, dl, Duration.ofMinutes(minutes), 0, goal);
    }

    @Test
    void noTasksGivesEmptyCoachState() {
        assertFalse(coach.analyzeCurrentSituation().hasTasks());
        assertNull(coach.recommendNextAction());
        assertTrue(coach.generateAlternatives().isEmpty());
        assertTrue(coach.generateAvoidList().isEmpty());
    }

    @Test
    void completedTasksAreExcluded() {
        tasks.save(task("done", Priority.HIGH, TaskStatus.DONE, TaskType.DEEP_WORK, 0, 30, 0));
        assertNull(coach.recommendNextAction());
    }

    @Test
    void urgentTaskIsRecommended() {
        tasks.save(task("Urgent", Priority.MEDIUM, TaskStatus.TODO, TaskType.ADMIN, 0, 30, 0));   // due today
        tasks.save(task("Far",    Priority.MEDIUM, TaskStatus.TODO, TaskType.ADMIN, 30, 30, 0));  // far away
        assertEquals("Urgent", coach.recommendNextAction().task().getTitle());
    }

    @Test
    void highPriorityTaskIsRecommended() {
        tasks.save(task("Hi", Priority.HIGH, TaskStatus.TODO, TaskType.ADMIN, null, 30, 0));
        tasks.save(task("Lo", Priority.LOW,  TaskStatus.TODO, TaskType.ADMIN, null, 30, 0));
        assertEquals("Hi", coach.recommendNextAction().task().getTitle());
    }

    @Test
    void taskLongerThanSelectedTimeIsNotRecommended() {
        tasks.save(task("Quick", Priority.MEDIUM, TaskStatus.TODO, TaskType.ADMIN, null, 10, 0));
        tasks.save(task("Long",  Priority.MEDIUM, TaskStatus.TODO, TaskType.ADMIN, null, 120, 0));
        CoachRecommendation best = coach.recommendForAvailableTime(AvailableTimeSlot.QUARTER); // 15 min
        assertEquals("Quick", best.task().getTitle());
    }

    @Test
    void lowEnergyPrefersLightTask() {
        tasks.save(task("Deep",  Priority.MEDIUM, TaskStatus.TODO, TaskType.DEEP_WORK, null, 50, 0));
        tasks.save(task("Light", Priority.MEDIUM, TaskStatus.TODO, TaskType.ADMIN, null, 20, 0));
        assertEquals("Light", coach.recommendForEnergyLevel(EnergyLevel.LOW).task().getTitle());
    }

    @Test
    void highEnergyCanRecommendDeepWork() {
        tasks.save(task("Deep",  Priority.MEDIUM, TaskStatus.TODO, TaskType.DEEP_WORK, null, 50, 0));
        tasks.save(task("Light", Priority.MEDIUM, TaskStatus.TODO, TaskType.ADMIN, null, 20, 0));
        assertEquals("Deep", coach.recommendForEnergyLevel(EnergyLevel.HIGH).task().getTitle());
    }

    @Test
    void recommendationHasReasons() {
        tasks.save(task("A", Priority.HIGH, TaskStatus.TODO, TaskType.ADMIN, 1, 30, 0));
        CoachRecommendation best = coach.recommendNextAction();
        assertFalse(best.reasons().isEmpty());
        assertTrue(best.reasons().stream().anyMatch(r -> r.lens().equals("Deadline")));
    }

    @Test
    void alternativesAreGenerated() {
        tasks.save(task("A", Priority.HIGH, TaskStatus.TODO, TaskType.ADMIN, 1, 30, 0));
        tasks.save(task("B", Priority.MEDIUM, TaskStatus.TODO, TaskType.ADMIN, 5, 30, 0));
        assertFalse(coach.generateAlternatives().isEmpty());
    }

    @Test
    void avoidListFlagsTooLongTask() {
        tasks.save(task("Quick", Priority.MEDIUM, TaskStatus.TODO, TaskType.ADMIN, null, 10, 0));
        tasks.save(task("Long",  Priority.MEDIUM, TaskStatus.TODO, TaskType.ADMIN, null, 120, 0));
        coach.setAvailableTime(AvailableTimeSlot.QUARTER);
        List<CoachAvoidItem> avoid = coach.generateAvoidList();
        assertTrue(avoid.stream().anyMatch(a -> a.reason().contains("longer than")));
    }

    @Test
    void missingGoalDataDegradesHonestly() {
        tasks.save(task("A", Priority.HIGH, TaskStatus.TODO, TaskType.ADMIN, 1, 30, 0));
        assertFalse(coach.analyzeCurrentSituation().goalsAvailable());
        CoachRecommendation best = coach.recommendNextAction();
        assertEquals("Not linked to a goal yet", best.goalContribution());
        CoachReason goalReason = best.reasons().stream()
                .filter(r -> r.lens().equals("Goal")).findFirst().orElseThrow();
        assertFalse(goalReason.realData(), "goal signal must be flagged as not real when no goal exists");
    }
}

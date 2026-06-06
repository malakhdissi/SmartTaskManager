package service;

import dao.InMemoryGoalDao;
import dao.InMemoryTaskDao;
import dao.InMemoryTemporalProfileDAO;
import dao.InMemoryTimeBlockDAO;
import model.AvailableTimeSlot;
import model.CoachRecommendation;
import model.EnergyLevel;
import model.Goal;
import model.GoalCategory;
import model.GoalStatus;
import model.Priority;
import model.Task;
import model.TaskStatus;
import model.TaskType;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import strategy.DefaultGoalContributionStrategy;
import strategy.DefaultScoringStrategy;
import strategy.GoalContributionStrategy;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/** Real, persistent Goal Engine: CRUD, task linking, contribution + computed progress, integrations. */
class GoalEngineTest {

    private Session session;
    private GoalService goals;
    private TaskService tasks;
    private GoalProgressService progress;

    @BeforeEach
    void setUp() {
        session = new Session();
        session.set(new User("u1", "Sam", "sam@example.com", null, 0, null));
        goals = new GoalService(new InMemoryGoalDao(session));
        tasks = new TaskServiceImpl(new InMemoryTaskDao(session), new DefaultScoringStrategy());
        progress = new GoalProgressService(tasks, new DefaultGoalContributionStrategy());
    }

    private Goal goal(String id, boolean active) {
        return new Goal(id, "Become SWE", "", GoalCategory.CAREER, 5, null, GoalStatus.ACTIVE, 0d, active);
    }

    private Task linkedTask(String id, String goalId, double contribution, TaskStatus status) {
        Task t = new Task(id, id, "", Priority.MEDIUM, status, TaskType.LEARNING,
                LocalDate.now().plusDays(1), Duration.ofMinutes(60), 0, contribution);
        t.setGoalId(goalId);
        return t;
    }

    @Test
    void createAndListGoal() {
        goals.save(goal("g1", false));
        assertEquals(1, goals.getAll().size());
        assertEquals("Become SWE", goals.getById("g1").getTitle());
    }

    @Test
    void editGoal() {
        goals.save(goal("g1", false));
        Goal g = goals.getById("g1");
        g.setTitle("Become Senior SWE");
        g.setImportance(4);
        goals.save(g);
        assertEquals("Become Senior SWE", goals.getById("g1").getTitle());
        assertEquals(4, goals.getById("g1").getImportance());
    }

    @Test
    void deleteGoal() {
        goals.save(goal("g1", false));
        assertTrue(goals.delete("g1"));
        assertNull(goals.getById("g1"));
    }

    @Test
    void linkTaskToGoal() {
        goals.save(goal("g1", true));
        tasks.save(linkedTask("t1", "g1", 0.3, TaskStatus.TODO));
        assertEquals(1, progress.linkedTasks("g1").size());
        assertEquals("g1", tasks.getById("t1").orElseThrow().getGoalId());
    }

    @Test
    void contributionIsZeroWhenNotLinked() {
        GoalContributionStrategy strat = new DefaultGoalContributionStrategy();
        Goal g = goal("g1", true);
        assertEquals(0.3, strat.contribution(linkedTask("t1", "g1", 0.3, TaskStatus.TODO), g), 0.0001);
        assertEquals(0.0, strat.contribution(linkedTask("t2", null, 0.9, TaskStatus.TODO), g), 0.0001);
        assertEquals(0.0, strat.contribution(linkedTask("t3", "other", 0.9, TaskStatus.TODO), g), 0.0001);
    }

    @Test
    void progressIsComputedFromCompletedLinkedTasks() {
        Goal g = goal("g1", true);
        goals.save(g);
        tasks.save(linkedTask("a", "g1", 0.20, TaskStatus.DONE)); // counts
        tasks.save(linkedTask("b", "g1", 0.10, TaskStatus.TODO)); // not done → ignored
        assertEquals(20, progress.progressPercent(g));
        assertEquals(2, progress.linkedCount(g));
        assertEquals(1, progress.completedCount(g));
    }

    @Test
    void progressIsZeroWithNoGoalOrNoTasks() {
        assertEquals(0, progress.progressPercent(null));
        assertEquals(0, progress.progressPercent(goal("g1", true)));
        assertTrue(goals.getAll().isEmpty());
    }

    @Test
    void recommendationConsidersGoalContribution() {
        DefaultScoringStrategy scoring = new DefaultScoringStrategy();
        Task low = linkedTask("low", "g1", 0.0, TaskStatus.TODO);
        Task high = linkedTask("high", "g1", 1.0, TaskStatus.TODO);
        assertTrue(scoring.score(high) > scoring.score(low), "higher goal contribution → higher score");
    }

    @Test
    void coachExplainsGoalContributionForLinkedTask() {
        goals.save(goal("g1", true));
        goals.setActive("g1");
        tasks.save(linkedTask("t1", "g1", 0.5, TaskStatus.TODO));

        AnalyticsService analytics = new AnalyticsService(tasks);
        TemporalPlanningService temporal = new TemporalPlanningService(
                new InMemoryTimeBlockDAO(session), new InMemoryTemporalProfileDAO(session), session);
        ProductivityCoachService coach = new ProductivityCoachService(tasks, goals, analytics, temporal);
        coach.setEnergy(EnergyLevel.HIGH);
        coach.setAvailableTime(AvailableTimeSlot.HOUR);

        // Progress is computed from completed linked tasks — the linked task is TODO, so 0% here.
        assertEquals(0, (int) coach.analyzeCurrentSituation().activeGoalProgress());
        CoachRecommendation best = coach.recommendNextAction();
        assertNotNull(best);
        var goalReason = best.reasons().stream().filter(r -> r.lens().equals("Goal")).findFirst().orElseThrow();
        assertTrue(goalReason.realData(), "linked task → real goal signal");
        assertTrue(goalReason.text().contains("Contributes"), "coach explains the contribution");
    }
}

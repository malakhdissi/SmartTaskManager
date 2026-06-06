package service;

import dao.Database;
import dao.GoalDao;
import dao.InMemoryGoalDao;
import dao.InMemoryTaskDao;
import dao.InMemoryTemporalProfileDAO;
import dao.InMemoryTimeBlockDAO;
import dao.InMemoryUserDao;
import dao.JdbcGoalDao;
import dao.JdbcTaskDao;
import dao.JdbcTemporalProfileDAO;
import dao.JdbcTimeBlockDAO;
import dao.JdbcUserDao;
import dao.TaskDao;
import dao.TemporalProfileDAO;
import dao.TimeBlockDAO;
import dao.UserDao;
import strategy.BasicTemporalRecommendationStrategy;
import strategy.DefaultRecommendationStrategy;
import strategy.DefaultScoringStrategy;
import strategy.RecommendationStrategy;
import strategy.ScoringStrategy;
import strategy.TemporalRecommendationStrategy;
import util.PasswordHasher;

/**
 * ServiceLocator — single source of truth for wiring DAOs + services.
 *
 * <p>On startup it attempts to bring the MySQL persistence layer online. If the
 * database is reachable, JDBC DAOs are used; otherwise the app falls back to
 * non-persistent in-memory DAOs so the UI still launches (data is lost on
 * exit). Either way, every DAO is user-scoped via the shared {@link Session}.</p>
 *
 * <p>The initialization order matters and is enforced by field declaration
 * order: session → persistence probe → DAOs → services.</p>
 */
public final class ServiceLocator {

    /* --- identity + security primitives --- */
    private static final Session SESSION = new Session();
    private static final PasswordHasher HASHER = new PasswordHasher();

    /* --- persistence probe (true = MySQL online) --- */
    private static final boolean PERSISTENCE_ONLINE = Database.init();

    /* --- DAOs: JDBC when online, in-memory fallback otherwise --- */
    private static final UserDao USER_DAO = PERSISTENCE_ONLINE ? new JdbcUserDao() : new InMemoryUserDao();
    private static final TaskDao TASK_DAO = PERSISTENCE_ONLINE ? new JdbcTaskDao(SESSION) : new InMemoryTaskDao(SESSION);
    private static final GoalDao GOAL_DAO = PERSISTENCE_ONLINE ? new JdbcGoalDao(SESSION) : new InMemoryGoalDao(SESSION);
    private static final TimeBlockDAO TIME_BLOCK_DAO = PERSISTENCE_ONLINE
            ? new JdbcTimeBlockDAO(SESSION) : new InMemoryTimeBlockDAO(SESSION);
    private static final TemporalProfileDAO TEMPORAL_PROFILE_DAO = PERSISTENCE_ONLINE
            ? new JdbcTemporalProfileDAO(SESSION) : new InMemoryTemporalProfileDAO(SESSION);

    /* --- strategies --- */
    private static final ScoringStrategy SCORING = new DefaultScoringStrategy();
    private static final RecommendationStrategy RECOMMENDATION = new DefaultRecommendationStrategy();
    private static final TemporalRecommendationStrategy TEMPORAL_STRATEGY = new BasicTemporalRecommendationStrategy();

    /* --- services --- */
    private static final TaskService TASK_SERVICE = new TaskServiceImpl(TASK_DAO, SCORING);
    private static final GoalService GOAL_SERVICE = new GoalService(GOAL_DAO);
    private static final GoalProgressService GOAL_PROGRESS_SERVICE =
            new GoalProgressService(TASK_SERVICE, new strategy.DefaultGoalContributionStrategy());
    private static final DashboardService DASHBOARD_SERVICE = new DashboardService(TASK_SERVICE, GOAL_SERVICE);
    private static final RecommendationService RECOMMENDATION_SERVICE =
            new RecommendationService(TASK_SERVICE, RECOMMENDATION);
    private static final AuthService AUTH_SERVICE = new AuthService(USER_DAO, HASHER, SESSION);
    private static final UserService USER_SERVICE = new UserService(SESSION, USER_DAO);
    private static final TemporalPlanningService TEMPORAL_PLANNING_SERVICE =
            new TemporalPlanningService(TIME_BLOCK_DAO, TEMPORAL_PROFILE_DAO, SESSION);
    private static final TemporalRecommendationService TEMPORAL_RECOMMENDATION_SERVICE =
            new TemporalRecommendationService(TASK_SERVICE, TEMPORAL_PLANNING_SERVICE, TEMPORAL_STRATEGY);
    private static final AnalyticsService ANALYTICS_SERVICE = new AnalyticsService(TASK_SERVICE);
    private static final ProductivityCoachService PRODUCTIVITY_COACH_SERVICE =
            new ProductivityCoachService(TASK_SERVICE, GOAL_SERVICE, ANALYTICS_SERVICE, TEMPORAL_PLANNING_SERVICE);
    private static final SmartScheduleService SMART_SCHEDULE_SERVICE = new SmartScheduleService(TASK_SERVICE);

    private ServiceLocator() {}

    public static TaskService taskService()                     { return TASK_SERVICE; }
    public static DashboardService dashboardService()           { return DASHBOARD_SERVICE; }
    public static RecommendationService recommendationService() { return RECOMMENDATION_SERVICE; }
    public static AuthService authService()                     { return AUTH_SERVICE; }
    public static UserService userService()                     { return USER_SERVICE; }
    public static GoalService goalService()                     { return GOAL_SERVICE; }
    public static GoalProgressService goalProgressService()     { return GOAL_PROGRESS_SERVICE; }
    public static TemporalPlanningService temporalPlanningService()             { return TEMPORAL_PLANNING_SERVICE; }
    public static TemporalRecommendationService temporalRecommendationService() { return TEMPORAL_RECOMMENDATION_SERVICE; }
    public static AnalyticsService analyticsService()                           { return ANALYTICS_SERVICE; }
    public static ProductivityCoachService productivityCoachService()           { return PRODUCTIVITY_COACH_SERVICE; }
    public static SmartScheduleService smartScheduleService()                   { return SMART_SCHEDULE_SERVICE; }

    /** The shared authentication session (used by controllers needing the current user id). */
    public static Session session()                             { return SESSION; }

    /** True when data is being persisted to MySQL; false in in-memory fallback. */
    public static boolean isPersistenceOnline()                 { return PERSISTENCE_ONLINE; }
}

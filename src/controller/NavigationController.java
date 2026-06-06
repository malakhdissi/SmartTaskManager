package controller;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import view.*;
import view.components.NotificationToast;

import java.util.function.Supplier;

/**
 * NavigationController — the single source of truth for screen swapping.
 *
 * <p>Every view talks to this controller to navigate. No view instantiates
 * another view directly. This pattern lets us later add transitions,
 * deep-linking, breadcrumbs, or guarded routes (e.g. "require login")
 * without touching screen code.</p>
 *
 * <p>Also hosts the toast overlay because it owns the root StackPane.</p>
 */
public class NavigationController {

    private final Stage stage;
    private final Scene scene;
    private final StackPane root;

    public NavigationController(Stage stage, Scene scene, StackPane root) {
        this.stage = stage;
        this.scene = scene;
        this.root = root;
    }

    /* ------------------------------------------------------------------
     * Internal helper: swap the visible root content while keeping the
     * overlay layer (toasts) intact at the top of the StackPane.
     * ------------------------------------------------------------------ */
    private void swap(Node next) {
        // Keep the first child slot as the "screen", any later children remain
        // (e.g. transient toasts mid-fade). Most of the time only screen is present.
        if (root.getChildren().isEmpty()) root.getChildren().add(next);
        else root.getChildren().set(0, next);
    }

    /**
     * Builds a screen and swaps it in, but never fails silently.
     *
     * <p>Previously a screen whose {@code build()} threw would abort the event
     * handler inside the JavaFX dispatcher, leaving the previous screen frozen
     * in place (the classic "login succeeds but nothing happens"). Now any
     * failure is logged with its root cause and surfaced as a danger toast, and
     * a readable fallback screen is shown instead of a blank window.</p>
     */
    private void render(String name, Supplier<Node> builder) {
        System.out.println("[Nav] navigation requested → " + name);
        try {
            Node node = builder.get();
            swap(node);
            System.out.println("[Nav] rendered ✓ " + name);
        } catch (Throwable t) {
            System.err.println("[Nav] FAILED to render " + name + ": " + t);
            t.printStackTrace();
            notifyDanger("Couldn't open " + name + " — " + rootCause(t));
            swap(errorScreen(name, t));
        }
    }

    private Node errorScreen(String name, Throwable t) {
        VBox box = new VBox(10);
        box.getStyleClass().add("content-area");
        Label title = new Label("Something went wrong opening " + name);
        title.getStyleClass().add("text-title");
        Label detail = new Label(rootCause(t));
        detail.getStyleClass().add("text-muted");
        detail.setWrapText(true);
        box.getChildren().addAll(title, detail);
        return box;
    }

    /** Deepest cause message — what actually broke, not the wrapper. */
    private static String rootCause(Throwable t) {
        Throwable c = t;
        while (c.getCause() != null) c = c.getCause();
        String msg = c.getMessage();
        return c.getClass().getSimpleName() + (msg == null ? "" : ": " + msg);
    }

    /** Exposed so views can publish toasts without keeping their own root ref. */
    public StackPane root() { return root; }

    public Stage stage() { return stage; }
    public Scene scene() { return scene; }

    /* ------------------------------------------------------------------
     * Auth / entry screens — full-bleed, no sidebar.
     * ------------------------------------------------------------------ */
    public void showWelcome() { render("Welcome", () -> new WelcomeView(this)); }
    public void showLogin()   { render("Login",   () -> new LoginView(this)); }
    public void showSignup()  { render("Sign up", () -> new SignupView(this)); }
    public void showForgotPassword() { render("Forgot password", () -> new ForgotPasswordView(this)); }

    /** Logs the user out and returns to the welcome screen. */
    public void logout() {
        service.ServiceLocator.authService().logout();
        System.out.println("[Auth] session cleared (logout)");
        showWelcome();
    }

    /* ------------------------------------------------------------------
     * MVP screens — wrapped in MainLayout shell.
     * ------------------------------------------------------------------ */
    public void showDashboard()    { render("Dashboard", () -> new MainLayout(this, new DashboardView(this).build(), "dashboard")); }
    public void showTaskList()     { render("Tasks",     () -> new MainLayout(this, new TaskListView(this).build(), "tasks")); }
    public void showAddTask()      { render("Add Task",  () -> new MainLayout(this, new AddTaskView(this).build(), "add-task")); }
    public void showTaskDetails(String id) {
        render("Task details", () -> new MainLayout(this, new TaskDetailsView(this, id).build(), "tasks"));
    }
    public void showEditTask(String id) {
        render("Edit task", () -> new MainLayout(this, new EditTaskView(this, id).build(), "tasks"));
    }
    public void showInsights()     { render("Insights", () -> new MainLayout(this, new InsightsView(this).build(), "insights")); }
    public void showSettings()     { render("Settings", () -> new MainLayout(this, new SettingsView(this).build(), "settings")); }
    public void showGoals()        { render("Goals",    () -> new MainLayout(this, new GoalDefinitionView(this).build(), "goals")); }

    /* ------------------------------------------------------------------
     * V1+ screens — wrapped in MainLayout shell except Deep Work.
     * ------------------------------------------------------------------ */
    public void showRecommendations() { render("Recommendations", () -> new MainLayout(this, new RecommendationEngineView(this).build(), "recommendations")); }
    public void showDeepWork()        { render("Deep Work", () -> new DeepWorkSessionView(this).build()); /* full bleed */ }
    public void showTimeline()        { render("Timeline", () -> new MainLayout(this, new ProductivityTimelineView(this).build(), "timeline")); }
    public void showHabits()          { render("Habits", () -> new MainLayout(this, new HabitTrackingView(this).build(), "habits")); }
    public void showDistractions()    { render("Distractions", () -> new MainLayout(this, new DistractionManagementView(this).build(), "distractions")); }

    /* ------------------------------------------------------------------
     * Future vision screens — wrapped in MainLayout shell.
     * ------------------------------------------------------------------ */
    public void showLeaderboard()    { render("Leaderboard", () -> new MainLayout(this, new LeaderboardView(this).build(), "leaderboard")); }
    public void showPersona()        { render("Persona", () -> new MainLayout(this, new PersonaAdaptationView(this).build(), "persona")); }
    public void showAiCoach()        { render("AI Coach", () -> new MainLayout(this, new AiCoachView(this).build(), "ai-coach")); }
    public void showSmartSchedule()  { render("Smart Schedule", () -> new MainLayout(this, new SmartScheduleGeneratorView(this).build(), "smart-schedule")); }
    public void showTemporal()       { render("Temporal Intelligence", () -> new MainLayout(this, new TemporalIntelligenceView(this).build(), "temporal")); }

    /* ------------------------------------------------------------------
     * User-feedback helpers — calm, non-blocking toasts.
     * ------------------------------------------------------------------ */
    public void notifyPrimary(String text)  { NotificationToast.show(root, text, NotificationToast.Kind.PRIMARY); }
    public void notifySuccess(String text)  { NotificationToast.show(root, text, NotificationToast.Kind.SUCCESS); }
    public void notifyWarning(String text)  { NotificationToast.show(root, text, NotificationToast.Kind.WARNING); }
    public void notifyDanger(String text)   { NotificationToast.show(root, text, NotificationToast.Kind.DANGER); }
}

# Appendix Code Snippets

These snippets are short presentation aids. They are not new source files and do not change application behavior.

## 1. Thin JavaFX Entry Point

Use this to explain that `Main` does not contain business logic.

```java
StackPane root = new StackPane();
Scene scene = new Scene(root, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

var css = getClass().getResource(Constants.CSS_PATH);
if (css != null) scene.getStylesheets().add(css.toExternalForm());

NavigationController nav = new NavigationController(stage, scene, root);
nav.showWelcome();
```

Talking point:
- JavaFX startup is separated from navigation, services, DAO, and business logic.

## 2. Centralized Navigation

Use this to explain that screens do not randomly instantiate each other.

```java
public void showDashboard() {
    render("Dashboard",
        () -> new MainLayout(this, new DashboardView(this).build(), "dashboard"));
}

public void showDeepWork() {
    render("Deep Work", () -> new DeepWorkSessionView(this).build());
}
```

Talking point:
- Most screens use the standard shell; Deep Work intentionally does not.

## 3. Controller Delegates to Service

Use this to explain UI/business separation.

```java
Task saved = tasks.save(t);
nav.notifySuccess("Task added: " + saved.getTitle());
nav.showTaskList();
```

Talking point:
- The view collects inputs; the controller calls the service and updates navigation/feedback.

## 4. DAO Abstraction

Use this to explain database evolutivity.

```java
private static final TaskDao TASK_DAO =
    PERSISTENCE_ONLINE ? new JdbcTaskDao(SESSION) : new InMemoryTaskDao(SESSION);
```

Talking point:
- The UI does not care whether the task comes from MySQL or in-memory fallback.

## 5. Strategy Pattern

Use this to explain intelligence modularity.

```java
private static final ScoringStrategy SCORING = new DefaultScoringStrategy();
private static final RecommendationStrategy RECOMMENDATION = new DefaultRecommendationStrategy();
private static final TemporalRecommendationStrategy TEMPORAL_STRATEGY =
    new BasicTemporalRecommendationStrategy();
```

Talking point:
- Current algorithms can later be replaced with stronger AI or analytics without rewriting JavaFX screens.

## 6. Ethical UX Empty State

Use this to explain honesty in product design.

```java
new EmptyState(
    "No insights available yet.",
    "Complete tasks to unlock analytics."
);
```

Talking point:
- The app avoids fake productivity analytics and uses transparent empty states.


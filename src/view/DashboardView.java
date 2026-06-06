package view;

import controller.NavigationController;
import controller.TaskUiController;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Kpi;
import model.Recommendation;
import model.Task;
import service.DashboardService;
import service.ServiceLocator;
import util.Constants;
import view.components.*;

import java.util.List;

/**
 * DashboardView — the most important screen.
 *
 * <p>Built to never crash: the shell always renders, and each data-backed
 * section (recommendation, KPIs, top tasks) is fetched defensively. If a
 * service or the database fails, that section degrades to a calm zero-state
 * and the technical cause is logged — the UI stays up.</p>
 *
 * <p>All numbers come from real {@link service.TaskService}/
 * {@link service.GoalService} data — never sample/mock data. A brand-new user
 * therefore sees honest zeros and clear "create your first task" prompts.</p>
 */
public class DashboardView {

    private final NavigationController nav;
    private final TaskUiController taskCtrl;

    public DashboardView(NavigationController nav) {
        this.nav = nav;
        this.taskCtrl = new TaskUiController(nav);
    }

    /** Builds the content node (without the layout shell). */
    public Node build() {
        VBox content = new VBox(20);

        // ----- Title -----
        ScreenTitle title = new ScreenTitle("Dashboard",
                "One best next action. Calm focus. No unfiltered backlog.");
        title.addAction(ActionButton.ghost("View insights").apply(b -> b.setOnAction(e -> nav.showInsights())));

        // ----- Best next action (null-safe; card renders an empty-state for null) -----
        RecommendationCard reco = new RecommendationCard(safeBestNext(), nav);

        // ----- KPI strip: compact + secondary (the hero leads, these support) -----
        FlowPane kpiRow = new FlowPane(12, 12);
        kpiRow.setAlignment(Pos.CENTER_LEFT);
        for (Kpi k : safeKpis()) {
            VBox tile = new VBox(2);
            tile.getStyleClass().add("kpi-compact");
            HBox.setHgrow(tile, Priority.ALWAYS);
            Label value = new Label(k.getValue());
            value.getStyleClass().add("kpi-compact-value");
            Label label = new Label(k.getLabel());
            label.getStyleClass().add("kpi-compact-label");
            tile.getChildren().addAll(value, label);
            kpiRow.getChildren().add(tile);
        }

        // ----- Two-column row: Top tasks + Chart -----
        HBox twoCols = new HBox(16);

        VBox left = new VBox(10);
        left.setMinWidth(360);
        left.getChildren().add(new ScreenTitle("Top " + Constants.DASHBOARD_TOP_TASKS + " Tasks",
                "Selected by score and goal contribution."));

        List<Task> top = safeTopTasks();
        if (top.isEmpty()) {
            left.getChildren().add(new EmptyState(
                    "No tasks yet",
                    "Create your first task to start building your productivity system.",
                    ActionButton.primary("Create First Task")
                            .apply(b -> b.setOnAction(e -> nav.showAddTask()))));
        } else {
            for (Task t : top) left.getChildren().add(new TaskCard(t, nav, taskCtrl));
        }
        HBox.setHgrow(left, Priority.ALWAYS);

        VBox right = new VBox(10);
        right.setMinWidth(420);
        right.getChildren().add(new ScreenTitle("Productivity Trend", "Focus over time"));
        VBox trendCard = new VBox(8);
        trendCard.getStyleClass().add("card");
        Label trendTitle = new Label("Trend appears once you have history");
        trendTitle.getStyleClass().add("text-body");
        Label trendHint = new Label("We don't show a sample chart. As you complete tasks and run focus "
                + "sessions, your real trend will build here.");
        trendHint.getStyleClass().add("text-muted");
        trendHint.setWrapText(true);
        trendCard.getChildren().addAll(trendTitle, trendHint);
        right.getChildren().add(trendCard);
        HBox.setHgrow(right, Priority.ALWAYS);

        twoCols.getChildren().addAll(left, right);

        // ----- Quick actions strip -----
        HBox quick = new HBox(10);
        quick.setAlignment(Pos.CENTER_LEFT);
        quick.getChildren().addAll(
                ActionButton.primary("+ Add Task").apply(b -> b.setOnAction(e -> nav.showAddTask())),
                ActionButton.ghost("Start Deep Work").apply(b -> b.setOnAction(e -> nav.showDeepWork())),
                ActionButton.ghost("View Recommendations").apply(b -> b.setOnAction(e -> nav.showRecommendations())),
                spacer(),
                ActionButton.neutral("Open AI Coach").apply(b -> b.setOnAction(e -> nav.showAiCoach()))
        );

        content.getChildren().addAll(title, reco, kpiRow, twoCols, quick);
        return content;
    }

    /* ---------------- defensive data accessors ---------------- */

    private Recommendation safeBestNext() {
        try {
            return ServiceLocator.recommendationService().getBestNext();
        } catch (Exception e) {
            System.err.println("[DashboardView] recommendation fetch failed: " + e);
            return null;
        }
    }

    private List<Kpi> safeKpis() {
        try {
            return ServiceLocator.dashboardService().getKpis();
        } catch (Exception e) {
            System.err.println("[DashboardView] KPI fetch failed, showing zero-state: " + e);
            return DashboardService.zeroStateKpis();
        }
    }

    private List<Task> safeTopTasks() {
        try {
            return ServiceLocator.taskService().getTopTasks(Constants.DASHBOARD_TOP_TASKS);
        } catch (Exception e) {
            System.err.println("[DashboardView] top-tasks fetch failed, showing empty-state: " + e);
            return List.of();
        }
    }

    private Region spacer() { Region r = new Region(); HBox.setHgrow(r, Priority.ALWAYS); return r; }
}

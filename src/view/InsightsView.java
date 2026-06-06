package view;

import controller.NavigationController;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Kpi;
import service.AnalyticsService;
import service.ServiceLocator;
import view.components.ActionButton;
import view.components.EmptyState;
import view.components.KpiCard;
import view.components.ScreenTitle;

import java.util.Map;

/**
 * InsightsView — analytics based entirely on the user's real tasks
 * (via {@link service.AnalyticsService}). No fabricated numbers: with no data
 * it shows an empty state; charts that need history are clearly labelled.
 */
public class InsightsView {

    private final NavigationController nav;

    public InsightsView(NavigationController nav) { this.nav = nav; }

    public Node build() {
        VBox root = new VBox(20);
        root.getChildren().add(new ScreenTitle("Insights",
                "Calm analytics from your real tasks. Trends matter more than single days."));

        AnalyticsService analytics = ServiceLocator.analyticsService();
        AnalyticsService.Snapshot snap = safeSnapshot(analytics);

        if (!snap.hasData()) {
            root.getChildren().add(new EmptyState(
                    "No insights yet",
                    "Create and complete tasks and your real productivity metrics will appear here.",
                    ActionButton.primary("Create a task").apply(b -> b.setOnAction(e -> nav.showAddTask()))));
            return root;
        }

        // ----- Real KPI row -----
        FlowPane kpiRow = new FlowPane(16, 16);
        addKpi(kpiRow, new Kpi("Tasks Completed", String.valueOf(snap.completed()), null, "success"));
        addKpi(kpiRow, new Kpi("Active Tasks", String.valueOf(snap.active()), null, "primary"));
        addKpi(kpiRow, new Kpi("Overdue", String.valueOf(snap.overdue()), null, "warning"));
        addKpi(kpiRow, new Kpi("Completion Rate", snap.completionRate() + "%", null, "intel"));
        addKpi(kpiRow, new Kpi("Deep Work", hours(snap.deepWorkMinutes()), null, "primary"));
        root.getChildren().add(kpiRow);

        // ----- Real distributions -----
        HBox distRow = new HBox(16);
        distRow.getChildren().addAll(
                distributionCard("Priority distribution (active)", analytics.priorityDistribution()),
                distributionCard("Status distribution (all)", analytics.statusDistribution()));
        root.getChildren().add(distRow);

        // ----- Weekly trend: honest about needing history -----
        VBox trend = new VBox(8);
        trend.getStyleClass().add("card");
        trend.getChildren().add(new ScreenTitle("Weekly trend", "Focus & completion over time"));
        Label trendNote = new Label("Weekly trends appear once there is enough day-to-day history "
                + "(completion timestamps are not yet tracked).");
        trendNote.getStyleClass().add("text-muted");
        trendNote.setWrapText(true);
        trend.getChildren().add(trendNote);
        root.getChildren().add(trend);

        return root;
    }

    private AnalyticsService.Snapshot safeSnapshot(AnalyticsService analytics) {
        try { return analytics.snapshot(); }
        catch (Exception e) {
            System.err.println("[InsightsView] snapshot failed: " + e);
            return new AnalyticsService.Snapshot(0, 0, 0, 0, 0, 0, false);
        }
    }

    private void addKpi(FlowPane row, Kpi kpi) {
        KpiCard card = new KpiCard(kpi);
        card.setMinWidth(200);
        HBox.setHgrow(card, Priority.ALWAYS);
        row.getChildren().add(card);
    }

    private Node distributionCard(String title, Map<String, Integer> data) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        card.setMinWidth(420);
        HBox.setHgrow(card, Priority.ALWAYS);

        Label t = new Label(title);
        t.getStyleClass().add("text-body");
        card.getChildren().add(t);

        int total = data.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) {
            Label none = new Label("No tasks to chart yet.");
            none.getStyleClass().add("text-muted");
            card.getChildren().add(none);
            return card;
        }
        for (Map.Entry<String, Integer> e : data.entrySet()) {
            double ratio = e.getValue() / (double) total;
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            Label name = new Label(e.getKey()); name.setMinWidth(110); name.getStyleClass().add("text-muted");
            ProgressBar pb = new ProgressBar(ratio); pb.setPrefWidth(220);
            Label count = new Label(e.getValue() + " (" + Math.round(ratio * 100) + "%)");
            count.getStyleClass().add("text-body");
            Region g = new Region(); HBox.setHgrow(g, Priority.ALWAYS);
            row.getChildren().addAll(name, pb, g, count);
            card.getChildren().add(row);
        }
        return card;
    }

    private static String hours(long minutes) {
        if (minutes <= 0) return "0h";
        double h = minutes / 60.0;
        return h == Math.floor(h) ? (long) h + "h" : String.format(java.util.Locale.US, "%.1fh", h);
    }
}

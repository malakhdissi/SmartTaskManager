package view;

import controller.NavigationController;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.TimelineEvent;
import service.ServiceLocator;
import util.Formatter;
import view.components.ActionButton;
import view.components.EmptyState;
import view.components.ScreenTitle;

import java.util.List;

/**
 * ProductivityTimelineView — chronological story built from REAL completed
 * tasks (via {@link service.AnalyticsService#recentTimeline()}). No invented
 * events: with nothing completed it shows an empty state.
 */
public class ProductivityTimelineView {

    private final NavigationController nav;
    public ProductivityTimelineView(NavigationController nav) { this.nav = nav; }

    public Node build() {
        VBox root = new VBox(16);
        root.getChildren().add(new ScreenTitle("Productivity Timeline",
                "What you actually completed. As you finish tasks, your activity appears here."));

        List<TimelineEvent> events = safeTimeline();
        if (events.isEmpty()) {
            root.getChildren().add(new EmptyState(
                    "No activity yet",
                    "Complete a task and it will appear on your timeline.",
                    ActionButton.primary("View tasks").apply(b -> b.setOnAction(e -> nav.showTaskList()))));
            return root;
        }

        VBox column = new VBox(0);
        column.getStyleClass().add("card");
        for (int i = 0; i < events.size(); i++) {
            column.getChildren().add(timelineRow(events.get(i)));
            if (i < events.size() - 1) column.getChildren().add(divider());
        }
        root.getChildren().add(column);

        Label note = new Label("Completion timestamps aren't tracked yet — entries show what you finished.");
        note.getStyleClass().add("text-muted");
        root.getChildren().add(note);
        return root;
    }

    private List<TimelineEvent> safeTimeline() {
        try { return ServiceLocator.analyticsService().recentTimeline(); }
        catch (Exception e) { System.err.println("[Timeline] fetch failed: " + e); return List.of(); }
    }

    private Node timelineRow(TimelineEvent e) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 10 0 10 0;");

        Region dot = new Region();
        String accent = switch (e.getKind()) {
            case TASK_COMPLETED      -> "dot-success";
            case FOCUS_SESSION       -> "dot-intel";
            case HABIT_KEPT          -> "dot-primary";
            case DISTRACTION_REDUCED -> "dot-warning";
        };
        dot.getStyleClass().addAll("dot", accent);

        VBox text = new VBox(2);
        Label title = new Label(e.getTitle());
        title.getStyleClass().add("text-body");
        Label sub = new Label(e.getSubtitle());
        sub.getStyleClass().add("text-muted");
        text.getChildren().addAll(title, sub);

        Region g = new Region(); HBox.setHgrow(g, Priority.ALWAYS);
        Label when = new Label(Formatter.time(e.getWhen()));
        when.getStyleClass().add("text-muted");

        row.getChildren().addAll(dot, text, g, when);
        return row;
    }

    private Region divider() {
        Region r = new Region();
        r.getStyleClass().add("divider");
        return r;
    }
}

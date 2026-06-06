package view.components;

import controller.NavigationController;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Recommendation;
import model.Task;
import util.Formatter;

/**
 * RecommendationCard — the Dashboard's "Today's Focus / One Best Next Action"
 * hero: a single recommended task with its reason and key metadata, plus the
 * primary Start-Focus and secondary View-Tasks actions.
 *
 * <p>Null-safe: with no recommendation it shows the honest empty state.</p>
 */
public class RecommendationCard extends VBox {

    public RecommendationCard(Recommendation rec, NavigationController nav) {
        getStyleClass().add("focus-hero");
        setSpacing(14);

        Label eyebrow = new Label("TODAY'S FOCUS · ONE BEST NEXT ACTION");
        eyebrow.getStyleClass().add("focus-eyebrow");

        if (rec == null) {
            Label title = new Label("No tasks yet");
            title.getStyleClass().add("focus-title");
            Label hint = new Label("Create your first task to start building your productivity system.");
            hint.getStyleClass().add("focus-reason");
            hint.setWrapText(true);
            ActionButton create = ActionButton.primary("Create First Task");
            create.setOnAction(e -> nav.showAddTask());
            getChildren().addAll(eyebrow, title, hint, create);
            return;
        }

        Task task = rec.getTask();
        Label title = new Label(task.getTitle());
        title.getStyleClass().add("focus-title");
        title.setWrapText(true);

        Label reason = new Label(rec.getReason());
        reason.getStyleClass().add("focus-reason");
        reason.setWrapText(true);

        // Meta chips: priority · deadline · duration · goal contribution
        FlowPane meta = new FlowPane(8, 8);
        if (task.getPriority() != null) meta.getChildren().add(chip(task.getPriority().label() + " priority"));
        if (task.getDeadline() != null) meta.getChildren().add(chip("Due " + Formatter.date(task.getDeadline())));
        if (task.getEstimatedDuration() != null) meta.getChildren().add(chip(Formatter.duration(task.getEstimatedDuration())));
        if (task.getGoalContribution() > 0)
            meta.getChildren().add(chip(Math.round(task.getGoalContribution() * 100) + "% to goal"));

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        ActionButton start = ActionButton.primary("Start Focus Session");
        start.setOnAction(e -> nav.showDeepWork());
        ActionButton viewTasks = ActionButton.ghost("View Tasks");
        viewTasks.setOnAction(e -> nav.showTaskList());
        Region g = new Region(); HBox.setHgrow(g, Priority.ALWAYS);
        Label confidence = new Label("Confidence " + Math.round(rec.getConfidence() * 100) + "%");
        confidence.getStyleClass().add("focus-meta-chip");
        actions.getChildren().addAll(start, viewTasks, g, confidence);

        getChildren().addAll(eyebrow, title, reason, meta, actions);
    }

    private Label chip(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("focus-meta-chip");
        return l;
    }
}

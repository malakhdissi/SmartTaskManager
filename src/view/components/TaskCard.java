package view.components;

import controller.NavigationController;
import controller.TaskUiController;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Task;
import util.Formatter;

/**
 * TaskCard — reusable visual for any list of tasks (Dashboard top 3, Task List, etc.).
 *
 * <p>Renders title, type, priority chip, deadline, score, and quick actions
 * (View, Mark Done). Wiring through the controllers keeps the UI logic-free.</p>
 */
public class TaskCard extends VBox {

    public TaskCard(Task task, NavigationController nav, TaskUiController taskCtrl) {
        getStyleClass().add("task-card");
        setSpacing(6);

        // ---- Top line: title + score ----
        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label(task.getTitle());
        title.getStyleClass().add("task-title");
        title.setWrapText(true);
        Region s = new Region();
        HBox.setHgrow(s, Priority.ALWAYS);
        Label score = new Label("Score " + Math.round(task.getScore()));
        score.getStyleClass().addAll("tag", "tag-intel");
        top.getChildren().addAll(title, s, score);

        // ---- Meta line: type + priority + deadline + status ----
        HBox meta = new HBox(8);
        meta.setAlignment(Pos.CENTER_LEFT);
        meta.getChildren().add(chip(task.getType() == null ? "—" : task.getType().label(), "tag-muted"));
        meta.getChildren().add(chip(task.getPriority() == null ? "—" : task.getPriority().label(),
                priorityVariant(task)));
        meta.getChildren().add(chip("Due " + Formatter.date(task.getDeadline()), "tag-muted"));
        meta.getChildren().add(chip(task.getStatus() == null ? "—" : task.getStatus().label(),
                statusVariant(task)));

        // ---- Actions line ----
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        ActionButton view = new ActionButton("View",   ActionButton.Variant.GHOST);
        view.getStyleClass().add("btn-icon");
        view.setOnAction(e -> nav.showTaskDetails(task.getId()));

        ActionButton edit = new ActionButton("Edit",   ActionButton.Variant.GHOST);
        edit.getStyleClass().add("btn-icon");
        edit.setOnAction(e -> nav.showEditTask(task.getId()));

        ActionButton done = new ActionButton("Mark Done", ActionButton.Variant.SUCCESS);
        done.getStyleClass().add("btn-icon");
        done.setOnAction(e -> taskCtrl.markDone(task.getId()));

        Region g = new Region();
        HBox.setHgrow(g, Priority.ALWAYS);
        actions.getChildren().addAll(g, view, edit, done);

        // Whole card is clickable as a shortcut to details — but buttons stop propagation.
        setOnMouseClicked(e -> {
            if (e.getTarget() == this || e.getTarget() == title) nav.showTaskDetails(task.getId());
        });

        getChildren().addAll(top, meta, actions);
    }

    private static Label chip(String text, String variantClass) {
        Label l = new Label(text);
        l.getStyleClass().addAll("tag", variantClass);
        return l;
    }

    private static String priorityVariant(Task t) {
        if (t.getPriority() == null) return "tag-muted";
        return switch (t.getPriority()) {
            case HIGH   -> "tag-danger";
            case MEDIUM -> "tag-warning";
            case LOW    -> "tag-primary";
        };
    }

    private static String statusVariant(Task t) {
        if (t.getStatus() == null) return "tag-muted";
        return switch (t.getStatus()) {
            case DONE        -> "tag-success";
            case IN_PROGRESS -> "tag-intel";
            case SKIPPED     -> "tag-muted";
            case TODO        -> "tag-primary";
        };
    }
}

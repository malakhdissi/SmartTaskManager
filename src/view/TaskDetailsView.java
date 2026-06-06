package view;

import controller.NavigationController;
import controller.TaskUiController;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Task;
import service.ServiceLocator;
import util.Formatter;
import view.components.*;

/**
 * TaskDetailsView — full information about a single task + actions.
 *
 * <p>Includes a "Why is this recommended?" block when the task's score is high.
 * Per-task activity history is shown as an honest "not tracked yet" note until
 * focus-session persistence lands.</p>
 */
public class TaskDetailsView {

    private final NavigationController nav;
    private final TaskUiController taskCtrl;
    private final String taskId;

    public TaskDetailsView(NavigationController nav, String taskId) {
        this.nav = nav;
        this.taskCtrl = new TaskUiController(nav);
        this.taskId = taskId;
    }

    public Node build() {
        Task task = ServiceLocator.taskService().getById(taskId).orElse(null);

        VBox root = new VBox(16);
        if (task == null) {
            root.getChildren().add(new ScreenTitle("Task not found", "It may have been deleted."));
            root.getChildren().add(new EmptyState("This task no longer exists.",
                    "Return to the task list to continue.",
                    ActionButton.primary("Back to tasks").apply(b -> b.setOnAction(e -> nav.showTaskList()))));
            return root;
        }

        ScreenTitle title = new ScreenTitle(task.getTitle(),
                "Task details · " + (task.getType() == null ? "—" : task.getType().label()));
        title.addAction(ActionButton.ghost("Edit").apply(b -> b.setOnAction(e -> nav.showEditTask(task.getId()))));
        title.addAction(ActionButton.primary("Start focus").apply(b -> b.setOnAction(e -> nav.showDeepWork())));
        title.addAction(ActionButton.success("Mark Done").apply(b -> b.setOnAction(e -> taskCtrl.markDone(task.getId()))));

        // ----- Key facts grid -----
        HBox factsRow = new HBox(16);
        factsRow.getChildren().addAll(
                fact("Priority", task.getPriority() == null ? "—" : task.getPriority().label()),
                fact("Status",   task.getStatus()   == null ? "—" : task.getStatus().label()),
                fact("Deadline", Formatter.date(task.getDeadline())),
                fact("Duration", Formatter.duration(task.getEstimatedDuration())),
                fact("Score",    Math.round(task.getScore()) + " / 100"),
                fact("Goal Contribution", Math.round(task.getGoalContribution() * 100) + "%")
        );

        // ----- Description -----
        VBox descCard = new VBox(6);
        descCard.getStyleClass().add("card");
        Label dt = new Label("Description"); dt.getStyleClass().add("text-muted");
        Label dv = new Label(task.getDescription() == null || task.getDescription().isBlank()
                ? "No description provided." : task.getDescription());
        dv.getStyleClass().add("text-body");
        dv.setWrapText(true);
        descCard.getChildren().addAll(dt, dv);

        // ----- Why-recommended explanation -----
        VBox whyCard = new VBox(6);
        whyCard.getStyleClass().add("card");
        Label wt = new Label("Why this might be a good next action"); wt.getStyleClass().add("recommendation-label");
        Label wv = new Label(explain(task));
        wv.getStyleClass().add("text-body");
        wv.setWrapText(true);
        whyCard.getChildren().addAll(wt, wv);

        // ----- Activity history (honest: not tracked yet) -----
        VBox historyCard = new VBox(6);
        historyCard.getStyleClass().add("card");
        Label ht = new Label("Recent activity"); ht.getStyleClass().add("text-muted");
        Label hv = new Label("Per-task activity history isn't tracked yet — it appears once focus-session "
                + "history is recorded.");
        hv.getStyleClass().add("text-muted");
        hv.setWrapText(true);
        historyCard.getChildren().addAll(ht, hv);

        root.getChildren().addAll(title, factsRow, descCard, whyCard, historyCard);
        return root;
    }

    private Node fact(String label, String value) {
        VBox v = new VBox(4);
        v.getStyleClass().add("card");
        Label l = new Label(label); l.getStyleClass().add("kpi-label");
        Label x = new Label(value); x.getStyleClass().add("text-body");
        v.getChildren().addAll(l, x);
        HBox.setHgrow(v, Priority.ALWAYS);
        return v;
    }

    private String explain(Task task) {
        StringBuilder sb = new StringBuilder();
        if (task.getGoalContribution() >= 0.6) {
            sb.append("Strong contribution (").append(Math.round(task.getGoalContribution() * 100))
              .append("%) to your active goal. ");
        }
        if (task.getDeadline() != null && task.getDeadline().isBefore(java.time.LocalDate.now().plusDays(2))) {
            sb.append("Deadline is close — best to clear this first. ");
        }
        if (task.getScore() >= 70) sb.append("Top of the list by composite score.");
        if (sb.length() == 0) sb.append("Calm choice when momentum is what you need.");
        return sb.toString();
    }
}

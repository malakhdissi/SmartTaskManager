package view;

import controller.NavigationController;
import controller.TaskUiController;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Task;
import model.TaskStatus;
import model.TaskType;
import service.ServiceLocator;
import view.components.ActionButton;
import view.components.EmptyState;
import view.components.ScreenTitle;

/**
 * EditTaskView — separate-screen variant of the edit form.
 *
 * <p>Same logic shape as {@link AddTaskView}: the view collects values,
 * the controller decides what to do. Pre-fills every field from the
 * currently-stored task.</p>
 */
public class EditTaskView {

    private final NavigationController nav;
    private final TaskUiController taskCtrl;
    private final String id;

    public EditTaskView(NavigationController nav, String id) {
        this.nav = nav;
        this.taskCtrl = new TaskUiController(nav);
        this.id = id;
    }

    public Node build() {
        Task t = ServiceLocator.taskService().getById(id).orElse(null);
        if (t == null) {
            VBox root = new VBox(16);
            root.getChildren().add(new ScreenTitle("Edit task", "Task not found."));
            root.getChildren().add(new EmptyState(
                    "This task no longer exists.",
                    "It might have been deleted in another tab.",
                    ActionButton.primary("Back to tasks").apply(b -> b.setOnAction(e -> nav.showTaskList()))));
            return root;
        }

        VBox root = new VBox(16);
        ScreenTitle title = new ScreenTitle("Edit Task", "Update fields and save your changes.");
        root.getChildren().add(title);

        TextField titleField = new TextField(t.getTitle());
        TextArea descField = new TextArea(t.getDescription());
        descField.setPrefRowCount(3);

        ComboBox<model.Priority> priority = new ComboBox<>(FXCollections.observableArrayList(model.Priority.values()));
        priority.getSelectionModel().select(t.getPriority());

        ComboBox<TaskType> type = new ComboBox<>(FXCollections.observableArrayList(TaskType.values()));
        type.getSelectionModel().select(t.getType());

        ComboBox<TaskStatus> status = new ComboBox<>(FXCollections.observableArrayList(TaskStatus.values()));
        status.getSelectionModel().select(t.getStatus());

        DatePicker deadline = new DatePicker(t.getDeadline());

        int initialMinutes = t.getEstimatedDuration() == null ? 30 : (int) t.getEstimatedDuration().toMinutes();
        Spinner<Integer> duration = new Spinner<>(5, 240, initialMinutes, 5);
        duration.setEditable(true);
        duration.setPrefWidth(120);

        HBox row1 = labeledRow("Title", titleField);
        HBox row2 = labeledRow("Description", descField);
        HBox row3 = new HBox(16, labeledRow("Priority", priority), labeledRow("Type", type), labeledRow("Status", status));
        HBox row4 = new HBox(16, labeledRow("Deadline", deadline), labeledRow("Duration (min)", duration));

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        ActionButton save = ActionButton.primary("Save changes");
        save.setOnAction(e -> taskCtrl.updateTask(
                id, titleField.getText(), descField.getText(),
                priority.getValue(), status.getValue(), type.getValue(),
                deadline.getValue(), duration.getValue() == null ? 30 : duration.getValue()));

        ActionButton cancel = ActionButton.ghost("Cancel");
        cancel.setOnAction(e -> nav.showTaskDetails(id));

        ActionButton delete = new ActionButton("Delete", ActionButton.Variant.DANGER);
        delete.setOnAction(e -> taskCtrl.delete(id));
        Region g = new Region(); HBox.setHgrow(g, Priority.ALWAYS);

        actions.getChildren().addAll(save, cancel, g, delete);

        root.getChildren().addAll(row1, row2, row3, row4, actions);
        return root;
    }

    private HBox labeledRow(String label, Node control) {
        Label l = new Label(label); l.getStyleClass().add("text-muted");
        VBox col = new VBox(6, l, control);
        if (control instanceof Region r) r.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(col, Priority.ALWAYS);
        return new HBox(col);
    }
}

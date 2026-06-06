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
import model.Goal;
import model.TaskStatus;
import model.TaskTemporalType;
import model.TaskType;
import service.ServiceLocator;
import util.Constants;
import view.components.ActionButton;
import view.components.ScreenTitle;

import java.time.LocalDate;

/**
 * AddTaskView — form to create a new task.
 *
 * <p>Smart UX: when the user selects DEEP_WORK, the duration field
 * recommends an uninterrupted block ({@link Constants#DEEP_WORK_DEFAULT_MINUTES}).
 * The view simply listens to the picker; it never decides scoring or persistence.</p>
 */
public class AddTaskView {

    private final NavigationController nav;
    private final TaskUiController taskCtrl;

    public AddTaskView(NavigationController nav) {
        this.nav = nav;
        this.taskCtrl = new TaskUiController(nav);
    }

    public Node build() {
        VBox root = new VBox(16);
        ScreenTitle title = new ScreenTitle("Capture a focused action",
                "Start with the action — add detail only if you need it.");
        root.getChildren().add(title);

        // ---- Form fields ----
        TextField titleField = new TextField();
        titleField.setPromptText("Task title");

        TextArea descField = new TextArea();
        descField.setPromptText("Optional description / context");
        descField.setPrefRowCount(3);

        ComboBox<model.Priority> priority = new ComboBox<>(FXCollections.observableArrayList(model.Priority.values()));
        priority.getSelectionModel().select(model.Priority.MEDIUM);

        ComboBox<TaskType> type = new ComboBox<>(FXCollections.observableArrayList(TaskType.values()));
        type.getSelectionModel().select(TaskType.LEARNING);

        ComboBox<TaskStatus> status = new ComboBox<>(FXCollections.observableArrayList(TaskStatus.values()));
        status.getSelectionModel().select(TaskStatus.TODO);

        DatePicker deadline = new DatePicker(LocalDate.now().plusDays(2));

        Spinner<Integer> duration = new Spinner<>(5, 240, 30, 5);
        duration.setEditable(true);
        duration.setPrefWidth(120);

        ComboBox<TaskTemporalType> temporal =
                new ComboBox<>(FXCollections.observableArrayList(TaskTemporalType.values()));
        temporal.setPromptText("Auto (from type)"); // unset → derived from TaskType

        // Goal linking (real goals from the DB; empty list → no goals yet)
        ComboBox<Goal> goalCombo = new ComboBox<>(
                FXCollections.observableArrayList(safeGoals()));
        goalCombo.setPromptText("No goal");
        goalCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Goal g) { return g == null ? "" : g.getTitle(); }
            @Override public Goal fromString(String s) { return null; }
        });
        Spinner<Integer> contribution = new Spinner<>(0, 100, 10, 5);
        contribution.setEditable(true);
        contribution.setPrefWidth(120);

        Label deepWorkHint = new Label("");
        deepWorkHint.getStyleClass().add("text-intel");

        // Smart hint: when DEEP_WORK is selected, suggest 90 minutes.
        type.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            if (b == TaskType.DEEP_WORK) {
                deepWorkHint.setText("Recommended uninterrupted duration: " + Constants.DEEP_WORK_DEFAULT_MINUTES + " minutes.");
                duration.getValueFactory().setValue(Constants.DEEP_WORK_DEFAULT_MINUTES);
            } else if (b == TaskType.SHORT_FOCUS) {
                deepWorkHint.setText("Short focus: try a 25-minute Pomodoro.");
                duration.getValueFactory().setValue(25);
            } else {
                deepWorkHint.setText("");
            }
        });

        // ---- Layout: primary field first, detail under progressive disclosure ----
        HBox row1 = labeledRow("What do you want to do?", titleField);
        HBox row2 = labeledRow("Description", descField);
        HBox row3 = new HBox(16, labeledRow("Priority", priority), labeledRow("Type", type), labeledRow("Status", status));
        HBox row4 = new HBox(16, labeledRow("Deadline", deadline), labeledRow("Duration (min)", duration),
                labeledRow("When (temporal type)", temporal));
        HBox row5 = new HBox(16, labeledRow("Goal", goalCombo), labeledRow("Contribution %", contribution));

        // Advanced fields are collapsed by default — reduce overload.
        VBox advanced = new VBox(16, row3, row4, row5);
        advanced.setManaged(false);
        advanced.setVisible(false);
        javafx.scene.control.Hyperlink more = new javafx.scene.control.Hyperlink("More options ▾");
        more.getStyleClass().add("disclosure-link");
        more.setOnAction(e -> {
            boolean show = !advanced.isVisible();
            advanced.setVisible(show);
            advanced.setManaged(show);
            more.setText(show ? "Fewer options ▴" : "More options ▾");
        });

        // ---- Inline validation message ----
        Label error = new Label();
        error.getStyleClass().add("auth-error");
        error.setWrapText(true);
        error.setManaged(false);
        error.setVisible(false);

        // ---- Actions ----
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        ActionButton save = ActionButton.primary("Save task");
        save.setOnAction(e -> {
            String taskTitle = titleField.getText();
            Integer dur = duration.getValue();
            if (taskTitle == null || taskTitle.isBlank()) { showError(error, "Title is required."); return; }
            if (dur == null || dur <= 0) { showError(error, "Duration must be a positive number of minutes."); return; }
            // Deadline is optional; the DatePicker only yields valid dates.
            clearError(error);
            Goal selectedGoal = goalCombo.getValue();
            String goalId = selectedGoal == null ? null : selectedGoal.getId();
            int contributionPct = contribution.getValue() == null ? 0 : contribution.getValue();
            taskCtrl.createTask(taskTitle, descField.getText(), priority.getValue(), status.getValue(),
                    type.getValue(), deadline.getValue(), dur, temporal.getValue(), goalId, contributionPct);
        });

        ActionButton cancel = ActionButton.ghost("Cancel");
        cancel.setOnAction(e -> nav.showTaskList());

        actions.getChildren().addAll(save, cancel);

        root.getChildren().addAll(row1, row2, more, advanced, deepWorkHint, error, actions);
        return root;
    }

    private java.util.List<Goal> safeGoals() {
        try { return ServiceLocator.goalService().getAll(); }
        catch (Exception e) { return java.util.List.of(); }
    }

    private void showError(Label error, String message) {
        error.setText(message);
        error.setManaged(true);
        error.setVisible(true);
    }

    private void clearError(Label error) {
        error.setText("");
        error.setManaged(false);
        error.setVisible(false);
    }

    /** Builds a stacked label + control column with calm spacing. */
    private HBox labeledRow(String label, Node control) {
        Label l = new Label(label);
        l.getStyleClass().add("text-muted");
        VBox col = new VBox(6, l, control);
        if (control instanceof Region r) r.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(col, Priority.ALWAYS);
        HBox row = new HBox(col);
        row.setMaxWidth(Double.MAX_VALUE);
        return row;
    }
}

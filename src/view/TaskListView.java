package view;

import controller.NavigationController;
import controller.TaskUiController;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Task;
import model.TaskStatus;
import service.ServiceLocator;
import view.components.*;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TaskListView — full searchable / filterable / sortable list of tasks.
 *
 * <p>UI logic only: gathers filter values and asks the service for tasks.
 * No SQL, no scoring, no recommendation logic here — those live in services.</p>
 */
public class TaskListView {

    private final NavigationController nav;
    private final TaskUiController taskCtrl;
    private final VBox listContainer = new VBox(10);

    private final TextField search = new TextField();
    private final ComboBox<String> priorityFilter = new ComboBox<>();
    private final ComboBox<String> statusFilter   = new ComboBox<>();
    private final ComboBox<String> sortBy         = new ComboBox<>();

    public TaskListView(NavigationController nav) {
        this.nav = nav;
        this.taskCtrl = new TaskUiController(nav);
    }

    public Node build() {
        VBox root = new VBox(16);

        ScreenTitle title = new ScreenTitle("Tasks",
                "Search, filter, and act on what matters. The score column reflects priority + urgency + goal contribution.");
        title.addAction(ActionButton.primary("+ Add Task").apply(b -> b.setOnAction(e -> nav.showAddTask())));

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        search.setPromptText("Search tasks…");
        search.setPrefWidth(280);
        search.textProperty().addListener((o, a, b) -> refresh());

        priorityFilter.setItems(FXCollections.observableArrayList("All priorities", "High", "Medium", "Low"));
        priorityFilter.getSelectionModel().selectFirst();
        priorityFilter.setOnAction(e -> refresh());

        statusFilter.setItems(FXCollections.observableArrayList("All statuses", "To do", "In progress", "Done", "Skipped"));
        statusFilter.getSelectionModel().selectFirst();
        statusFilter.setOnAction(e -> refresh());

        sortBy.setItems(FXCollections.observableArrayList("Sort: Score (high → low)", "Sort: Deadline", "Sort: Title"));
        sortBy.getSelectionModel().selectFirst();
        sortBy.setOnAction(e -> refresh());

        Region g = new Region(); HBox.setHgrow(g, Priority.ALWAYS);
        toolbar.getChildren().addAll(search, priorityFilter, statusFilter, g, sortBy);

        listContainer.setSpacing(10);
        refresh();

        root.getChildren().addAll(title, toolbar, listContainer);
        return root;
    }

    /** Pulls tasks from the service, applies UI-side filtering, repaints the list. */
    private void refresh() {
        listContainer.getChildren().clear();
        List<Task> tasks = ServiceLocator.taskService().getAllTasks();

        String q = search.getText() == null ? "" : search.getText().toLowerCase().trim();
        String pf = priorityFilter.getValue();
        String sf = statusFilter.getValue();
        String sb = sortBy.getValue();

        List<Task> filtered = tasks.stream()
                .filter(t -> q.isEmpty() || t.getTitle().toLowerCase().contains(q)
                        || (t.getDescription() != null && t.getDescription().toLowerCase().contains(q)))
                .filter(t -> pf == null || pf.startsWith("All") || matchPriority(t, pf))
                .filter(t -> sf == null || sf.startsWith("All") || matchStatus(t, sf))
                .collect(Collectors.toList());

        Comparator<Task> cmp;
        if (sb == null || sb.startsWith("Sort: Score")) {
            cmp = Comparator.comparingDouble(Task::getScore).reversed();
        } else if (sb.startsWith("Sort: Deadline")) {
            cmp = Comparator.comparing((Task t) ->
                    t.getDeadline() == null ? java.time.LocalDate.MAX : t.getDeadline());
        } else {
            cmp = Comparator.comparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER);
        }
        filtered.sort(cmp);

        if (filtered.isEmpty()) {
            listContainer.getChildren().add(new EmptyState(
                    "No tasks match your filters.",
                    "Try clearing the search, or create your first focused action.",
                    ActionButton.primary("+ Add Task").apply(b -> b.setOnAction(e -> nav.showAddTask()))));
            return;
        }
        for (Task t : filtered) listContainer.getChildren().add(new TaskCard(t, nav, taskCtrl));
    }

    private boolean matchPriority(Task t, String label) {
        if (t.getPriority() == null) return false;
        return t.getPriority() == model.Priority.valueOf(label.toUpperCase());
    }
    private boolean matchStatus(Task t, String label) {
        if (t.getStatus() == null) return false;
        return switch (label) {
            case "To do"       -> t.getStatus() == TaskStatus.TODO;
            case "In progress" -> t.getStatus() == TaskStatus.IN_PROGRESS;
            case "Done"        -> t.getStatus() == TaskStatus.DONE;
            case "Skipped"     -> t.getStatus() == TaskStatus.SKIPPED;
            default            -> true;
        };
    }
}

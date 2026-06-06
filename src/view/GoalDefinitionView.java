package view;

import controller.GoalController;
import controller.NavigationController;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Goal;
import model.GoalCategory;
import model.GoalStatus;
import service.ServiceLocator;
import util.Formatter;
import view.components.ActionButton;
import view.components.EmptyState;
import view.components.ScreenTitle;

import java.util.List;

/**
 * GoalDefinitionView — full Goals management: create / edit / delete / set
 * active, with progress computed from real linked tasks (no stored/fake
 * progress) and the count of linked tasks. Empty state when no goals exist.
 */
public class GoalDefinitionView {

    private final NavigationController nav;
    private final GoalController controller;

    public GoalDefinitionView(NavigationController nav) {
        this.nav = nav;
        this.controller = new GoalController(nav);
    }

    public Node build() {
        VBox root = new VBox(16);
        root.getChildren().add(new ScreenTitle("Your Goals",
                "Define north-star goals. Progress is computed from the tasks you complete toward each one."));

        root.getChildren().add(form());

        List<Goal> goals = safeGoals();
        if (goals.isEmpty()) {
            root.getChildren().add(new EmptyState(
                    "No goals yet",
                    "Create a goal above, then link tasks to it so your progress builds automatically."));
            return root;
        }
        VBox list = new VBox(12);
        for (Goal g : goals) list.getChildren().add(goalCard(g));
        root.getChildren().add(list);
        return root;
    }

    /* ---------------- create / edit form ---------------- */

    private Node form() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        Label heading = new Label("Define a goal");
        heading.getStyleClass().add("text-body");

        TextField title = new TextField();
        title.setPromptText("e.g. Become a software engineer");
        TextArea desc = new TextArea();
        desc.setPromptText("Why does this matter? What does success look like?");
        desc.setPrefRowCount(2);
        ComboBox<GoalCategory> category = new ComboBox<>(FXCollections.observableArrayList(GoalCategory.values()));
        category.getSelectionModel().select(GoalCategory.CAREER);
        Spinner<Integer> importance = new Spinner<>(1, 5, 3, 1);
        importance.setPrefWidth(90);
        DatePicker targetDate = new DatePicker();
        targetDate.setPromptText("Target date (optional)");

        final String[] editingId = {null};
        ActionButton save = ActionButton.primary("Create goal");
        save.setOnAction(e -> {
            if (editingId[0] == null) {
                controller.create(title.getText(), desc.getText(), category.getValue(),
                        importance.getValue() == null ? 3 : importance.getValue(), targetDate.getValue());
            } else {
                controller.update(editingId[0], title.getText(), desc.getText(), category.getValue(),
                        importance.getValue() == null ? 3 : importance.getValue(), targetDate.getValue(), GoalStatus.ACTIVE);
            }
        });

        // Edit buttons (built in goalCard) populate this form via this hook.
        this.populate = g -> {
            editingId[0] = g.getId();
            title.setText(g.getTitle());
            desc.setText(g.getDescription());
            category.getSelectionModel().select(g.getCategory());
            importance.getValueFactory().setValue(g.getImportance());
            targetDate.setValue(g.getTargetDate());
            save.setText("Update goal");
        };

        HBox row1 = labeledRow("Title", title);
        HBox row2 = labeledRow("Description", desc);
        HBox row3 = new HBox(16, labeledRow("Category", category), labeledRow("Importance (1–5)", importance),
                labeledRow("Target date", targetDate));
        card.getChildren().addAll(heading, row1, row2, row3, save);
        return card;
    }

    /** Set by {@link #form()} so goal-card Edit buttons can refill the form. */
    private java.util.function.Consumer<Goal> populate = g -> { };

    /* ---------------- goal card ---------------- */

    private Node goalCard(Goal g) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card-elevated");

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        Label name = new Label(g.getTitle());
        name.getStyleClass().add("text-body");
        Label badge = new Label(g.isActive() ? "Active" : g.getStatus().label());
        badge.getStyleClass().addAll("tag", g.isActive() ? "tag-success" : "tag-muted");
        Label cat = new Label(g.getCategory().label() + " · importance " + g.getImportance()
                + (g.getTargetDate() == null ? "" : " · by " + Formatter.date(g.getTargetDate())));
        cat.getStyleClass().add("text-muted");
        Region grow = new Region(); HBox.setHgrow(grow, Priority.ALWAYS);
        top.getChildren().addAll(name, badge, grow, cat);

        int pct = ServiceLocator.goalProgressService().progressPercent(g);
        int linked = ServiceLocator.goalProgressService().linkedCount(g);
        int done = ServiceLocator.goalProgressService().completedCount(g);
        ProgressBar bar = new ProgressBar(pct / 100.0);
        bar.setMaxWidth(Double.MAX_VALUE);
        Label stat = new Label(pct + "% · " + linked + " linked task(s) · " + done + " done");
        stat.getStyleClass().add("text-muted");

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);
        ActionButton setActive = ActionButton.ghost(g.isActive() ? "Active goal" : "Set active");
        setActive.setDisable(g.isActive());
        setActive.setOnAction(e -> controller.setActive(g.getId()));
        ActionButton edit = new ActionButton("Edit", ActionButton.Variant.NEUTRAL);
        edit.setOnAction(e -> populate.accept(g));
        ActionButton delete = new ActionButton("Delete", ActionButton.Variant.DANGER);
        delete.setOnAction(e -> controller.delete(g.getId()));
        actions.getChildren().addAll(setActive, edit, delete);

        card.getChildren().addAll(top, bar, stat, actions);
        return card;
    }

    /* ---------------- helpers ---------------- */

    private List<Goal> safeGoals() {
        try { return ServiceLocator.goalService().getAll(); }
        catch (Exception e) { System.err.println("[Goals] fetch failed: " + e); return List.of(); }
    }

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

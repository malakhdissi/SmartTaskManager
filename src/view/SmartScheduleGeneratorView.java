package view;

import controller.NavigationController;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.ScheduleBlock;
import service.ServiceLocator;
import view.components.ActionButton;
import view.components.EmptyState;
import view.components.ScreenTitle;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * SmartScheduleGeneratorView — a day plan built from the user's REAL active
 * tasks (via {@link service.SmartScheduleService}), never invented tasks.
 *
 * <p>If there are no tasks, it shows an empty state instead of a fake plan.</p>
 */
public class SmartScheduleGeneratorView {

    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");
    private final NavigationController nav;

    public SmartScheduleGeneratorView(NavigationController nav) { this.nav = nav; }

    public Node build() {
        VBox root = new VBox(16);
        ScreenTitle title = new ScreenTitle("Smart Schedule",
                "A suggested day plan generated from your real tasks — ordered by score, sized by duration.");
        title.addAction(ActionButton.ghost("Regenerate")
                .apply(b -> b.setOnAction(e -> nav.showSmartSchedule())));
        root.getChildren().add(title);

        List<ScheduleBlock> blocks = safeGenerate();
        if (blocks.isEmpty()) {
            root.getChildren().add(new EmptyState(
                    "No schedule yet",
                    "Create tasks to generate your first smart schedule. The plan uses each task's "
                            + "duration, deadline, priority and type.",
                    ActionButton.primary("Create a task").apply(b -> b.setOnAction(e -> nav.showAddTask()))));
            return root;
        }

        VBox card = new VBox(0);
        card.getStyleClass().add("card");
        for (ScheduleBlock b : blocks) card.getChildren().add(blockRow(b));
        root.getChildren().add(card);

        Label note = new Label("Built from your active tasks, highest-impact first, with short breaks between blocks.");
        note.getStyleClass().add("text-muted");
        root.getChildren().add(note);
        return root;
    }

    private List<ScheduleBlock> safeGenerate() {
        try { return ServiceLocator.smartScheduleService().generate(); }
        catch (Exception e) { System.err.println("[SmartSchedule] generate failed: " + e); return List.of(); }
    }

    private Node blockRow(ScheduleBlock b) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 12 0 12 0;");

        Label time = new Label(b.getStart().format(HM) + " — " + b.getEnd().format(HM));
        time.getStyleClass().add("text-muted");
        time.setMinWidth(120);

        Region dot = new Region();
        dot.getStyleClass().addAll("dot", "dot-" + b.getAccent());

        Label label = new Label(b.getLabel());
        label.getStyleClass().add("text-body");
        label.setWrapText(true);

        Region g = new Region(); HBox.setHgrow(g, Priority.ALWAYS);

        Label kind = new Label(b.getBlockType() == null ? "Task" : b.getBlockType().label());
        kind.getStyleClass().addAll("tag", "tag-" + b.getAccent());

        row.getChildren().addAll(time, dot, label, g, kind);
        return row;
    }
}

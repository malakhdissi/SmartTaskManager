package view;

import controller.NavigationController;
import controller.TemporalController;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Chronotype;
import model.DayPeriod;
import model.EnergyLevel;
import model.Recommendation;
import model.Task;
import model.TaskTemporalType;
import model.TemporalProfile;
import model.TimeBlock;
import service.ServiceLocator;
import view.components.ActionButton;
import view.components.EmptyState;
import view.components.ScreenTitle;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * TemporalIntelligenceView — real temporal planning surface.
 *
 * <p>Shows the single best task to do now, the user's editable energy profile,
 * detected deep-work windows, time blocks, and tasks grouped by temporal type.
 * Every data read is defensive: a service/DB error degrades to a calm message
 * rather than crashing the screen.</p>
 */
public class TemporalIntelligenceView {

    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    private final NavigationController nav;

    public TemporalIntelligenceView(NavigationController nav) { this.nav = nav; }

    public Node build() {
        VBox root = new VBox(16);
        root.getChildren().add(new ScreenTitle("Temporal Intelligence",
                "Match the right task to the right energy and time — without splitting work that must stay whole."));

        root.getChildren().add(bestNowCard());

        HBox cols = new HBox(16);
        cols.getChildren().addAll(profileCard(), blocksCard());
        root.getChildren().add(cols);

        root.getChildren().add(buckets());
        return root;
    }

    /* ---------------- best task now ---------------- */

    private Node bestNowCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("recommendation-card");

        Label label = new Label("BEST TASK TO DO NOW");
        label.getStyleClass().add("recommendation-label");
        card.getChildren().add(label);

        Optional<Recommendation> rec = safe(
                () -> ServiceLocator.temporalRecommendationService().bestTaskNow(), Optional.empty());

        if (rec.isEmpty()) {
            Label t = new Label("Nothing to recommend yet");
            t.getStyleClass().add("recommendation-title");
            Label h = new Label("Add tasks and set their temporal type to plan your day around your energy.");
            h.getStyleClass().add("recommendation-reason");
            h.setWrapText(true);
            ActionButton add = ActionButton.primary("Add a task");
            add.setOnAction(e -> nav.showAddTask());
            card.getChildren().addAll(t, h, add);
            return card;
        }

        Recommendation r = rec.get();
        Label title = new Label(r.getTask().getTitle());
        title.getStyleClass().add("recommendation-title");
        title.setWrapText(true);
        Label reason = new Label("Why: " + r.getReason());
        reason.getStyleClass().add("recommendation-reason");
        reason.setWrapText(true);

        HBox conf = new HBox(8);
        conf.setAlignment(Pos.CENTER_LEFT);
        Label cl = new Label("Confidence");
        cl.getStyleClass().add("text-muted");
        ProgressBar bar = new ProgressBar(r.getConfidence());
        bar.setPrefWidth(160);
        ActionButton start = ActionButton.primary("Start focus");
        start.setOnAction(e -> nav.showDeepWork());
        Region g = new Region(); HBox.setHgrow(g, Priority.ALWAYS);
        conf.getChildren().addAll(start, g, cl, bar);

        card.getChildren().addAll(title, reason, conf);
        return card;
    }

    /* ---------------- editable energy profile ---------------- */

    private Node profileCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setMinWidth(360);
        card.getChildren().add(sectionLabel("Your energy profile"));

        TemporalProfile p = safe(() -> ServiceLocator.temporalPlanningService().getProfile(), null);
        if (p == null) {
            card.getChildren().add(new Label("Sign in to set your energy profile.") {{ getStyleClass().add("text-muted"); }});
            return card;
        }

        ComboBox<EnergyLevel> morning = energyCombo(p.getPreferredMorningEnergy());
        ComboBox<EnergyLevel> afternoon = energyCombo(p.getPreferredAfternoonEnergy());
        ComboBox<EnergyLevel> evening = energyCombo(p.getPreferredEveningEnergy());
        ComboBox<Chronotype> chronotype = new ComboBox<>(FXCollections.observableArrayList(Chronotype.values()));
        chronotype.getSelectionModel().select(p.getChronotype());
        ComboBox<DayPeriod> bestDeep = new ComboBox<>(FXCollections.observableArrayList(DayPeriod.values()));
        bestDeep.getSelectionModel().select(p.getBestDeepWorkPeriod());
        ComboBox<DayPeriod> fatigue = new ComboBox<>(FXCollections.observableArrayList(DayPeriod.values()));
        fatigue.getSelectionModel().select(p.getFatiguePeriod());

        card.getChildren().addAll(
                field("Morning energy", morning),
                field("Afternoon energy", afternoon),
                field("Evening energy", evening),
                field("Chronotype", chronotype),
                field("Best deep-work period", bestDeep),
                field("Fatigue period", fatigue));

        ActionButton save = ActionButton.primary("Save profile");
        save.setOnAction(e -> new TemporalController(nav).saveProfile(
                morning.getValue(), afternoon.getValue(), evening.getValue(),
                chronotype.getValue(), bestDeep.getValue(), fatigue.getValue()));
        card.getChildren().add(save);
        return card;
    }

    /* ---------------- time blocks + deep-work windows ---------------- */

    private Node blocksCard() {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setMinWidth(360);
        card.getChildren().add(sectionLabel("Your time blocks"));

        List<TimeBlock> blocks = safe(() -> ServiceLocator.temporalPlanningService().getBlocks(), List.of());
        List<TimeBlock> deep = safe(() -> ServiceLocator.temporalPlanningService().detectDeepWorkWindows(), List.of());

        if (blocks.isEmpty()) {
            card.getChildren().add(new Label("No time blocks yet.") {{ getStyleClass().add("text-muted"); }});
            return card;
        }
        for (TimeBlock b : blocks) {
            boolean isDeep = deep.contains(b);
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            Label time = new Label(b.getStartTime().format(HM) + "–" + b.getEndTime().format(HM));
            time.setMinWidth(110);
            time.getStyleClass().add("text-body");
            Label lbl = new Label(b.getLabel() == null ? "" : b.getLabel());
            lbl.getStyleClass().add("text-muted");
            Region g = new Region(); HBox.setHgrow(g, Priority.ALWAYS);
            Label energy = new Label(b.getEnergyLevel().label() + " · " + b.getAvailableMinutes() + "m");
            energy.getStyleClass().addAll("tag", isDeep ? "tag-success" : "tag-muted");
            row.getChildren().addAll(time, lbl, g, energy);
            card.getChildren().add(row);
        }
        if (!deep.isEmpty()) {
            Label hint = new Label(deep.size() + " deep-work window(s) detected (high energy, 60+ min).");
            hint.getStyleClass().add("text-success");
            card.getChildren().add(hint);
        }
        return card;
    }

    /* ---------------- tasks grouped by temporal type ---------------- */

    private Node buckets() {
        FlowPane row = new FlowPane(16, 16);
        row.getChildren().addAll(
                bucket("Fixed-time", TaskTemporalType.FIXED_TIME, "Must happen at a set time."),
                bucket("Deep work", TaskTemporalType.DEEP_WORK, "Needs a protected focus window."),
                bucket("Indivisible", TaskTemporalType.INDIVISIBLE, "Never split — one sitting."),
                bucket("Flexible", TaskTemporalType.FLEXIBLE, "Fit into any gap."));
        return row;
    }

    private Node bucket(String title, TaskTemporalType type, String subtitle) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card-elevated");
        card.setMinWidth(240);
        HBox.setHgrow(card, Priority.ALWAYS);
        card.getChildren().add(sectionLabel(title));
        Label sub = new Label(subtitle);
        sub.getStyleClass().add("text-muted");
        sub.setWrapText(true);
        card.getChildren().add(sub);

        List<Task> tasks = safe(
                () -> ServiceLocator.temporalRecommendationService().tasksOfTemporalType(type), List.of());
        if (tasks.isEmpty()) {
            Label none = new Label("None");
            none.getStyleClass().add("text-muted");
            card.getChildren().add(none);
        } else {
            for (Task t : tasks) {
                Label l = new Label("• " + t.getTitle());
                l.getStyleClass().add("text-body");
                l.setWrapText(true);
                card.getChildren().add(l);
            }
        }
        return card;
    }

    /* ---------------- small helpers ---------------- */

    private Label sectionLabel(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("text-body");
        return l;
    }

    private ComboBox<EnergyLevel> energyCombo(EnergyLevel selected) {
        ComboBox<EnergyLevel> c = new ComboBox<>(FXCollections.observableArrayList(EnergyLevel.values()));
        c.getSelectionModel().select(selected);
        return c;
    }

    private HBox field(String label, Node control) {
        Label l = new Label(label);
        l.getStyleClass().add("text-muted");
        l.setMinWidth(180);
        Region g = new Region(); HBox.setHgrow(g, Priority.ALWAYS);
        HBox row = new HBox(10, l, g, control);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /** Runs a data fetch, returning a fallback (and logging) on any failure. */
    private <T> T safe(Supplier<T> supplier, T fallback) {
        try {
            return supplier.get();
        } catch (Exception e) {
            System.err.println("[TemporalIntelligenceView] data fetch failed: " + e);
            return fallback;
        }
    }
}

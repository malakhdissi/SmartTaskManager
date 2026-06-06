package view;

import controller.CoachController;
import controller.NavigationController;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.AvailableTimeSlot;
import model.CoachAvoidItem;
import model.CoachContext;
import model.CoachInsight;
import model.CoachReason;
import model.CoachRecommendation;
import model.EnergyLevel;
import view.components.ActionButton;
import view.components.EmptyState;
import view.components.ScreenTitle;

import java.util.List;

/**
 * AiCoachView — the Productivity Decision Coach. A real strategist screen
 * (not a chatbot, not a single card): current situation, best next action with
 * reasoning, "if I only have…" and energy controls that re-compute live,
 * alternatives, an avoid list, and real insights. Every value comes from
 * {@link controller.CoachController} → {@code ProductivityCoachService}.
 */
public class AiCoachView {

    private final NavigationController nav;
    private final CoachController coach;
    private final VBox root = new VBox(16);

    public AiCoachView(NavigationController nav) {
        this.nav = nav;
        this.coach = new CoachController();
    }

    public Node build() {
        render();
        return root;
    }

    private void render() {
        root.getChildren().clear();
        root.getChildren().add(new ScreenTitle("AI Coach",
                "Your productivity strategist — every recommendation is computed from your real data and explained."));

        CoachContext ctx = safe(coach::situation, null);
        if (ctx == null || !ctx.hasTasks()) {
            root.getChildren().add(new EmptyState(
                    "Nothing to coach yet",
                    "Add tasks so I can recommend your next best action.",
                    ActionButton.primary("Create a task").apply(b -> b.setOnAction(e -> nav.showAddTask()))));
            return;
        }

        root.getChildren().add(currentSituation(ctx));
        CoachRecommendation best = safe(coach::best, null);
        root.getChildren().add(bestNextAction(best));
        if (best != null) root.getChildren().add(whyThisAction(best));
        root.getChildren().add(controlsRow());     // If I only have… + Energy mode
        root.getChildren().add(alternatives());
        root.getChildren().add(avoidNow());
        root.getChildren().add(insights(ctx));
    }

    /* ---------------- 1. current situation ---------------- */

    private Node currentSituation(CoachContext ctx) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.getChildren().add(section("CURRENT SITUATION"));
        FlowPane grid = new FlowPane(24, 6);
        grid.getChildren().addAll(
                stat("Active tasks", String.valueOf(ctx.activeTasks().size())),
                stat("Overdue", String.valueOf(ctx.overdue())),
                stat("Completed", String.valueOf(ctx.completed())),
                stat("Energy", ctx.energy().label()),
                stat("Available", ctx.availableMinutes() + " min"),
                stat("Day period", ctx.period().label()),
                stat("Active goal", ctx.activeGoalTitle() == null ? "None linked" : ctx.activeGoalTitle()));
        card.getChildren().add(grid);
        if (!ctx.goalsAvailable()) {
            Label note = new Label("Link tasks to goals to improve goal-based recommendations.");
            note.getStyleClass().add("text-muted");
            note.setWrapText(true);
            card.getChildren().add(note);
        }
        return card;
    }

    /* ---------------- 2. best next action ---------------- */

    private Node bestNextAction(CoachRecommendation best) {
        VBox card = new VBox(12);
        card.getStyleClass().add("focus-hero");
        card.getChildren().add(label("BEST NEXT ACTION", "focus-eyebrow"));
        if (best == null) {
            card.getChildren().add(label("Nothing fits right now", "focus-title"));
            card.getChildren().add(label("Adjust your available time or energy below.", "focus-reason"));
            return card;
        }
        Label title = label(best.task().getTitle(), "focus-title");
        title.setWrapText(true);
        FlowPane meta = new FlowPane(8, 8);
        meta.getChildren().addAll(chip(best.priorityLabel() + " priority"), chip(best.deadlineLabel()),
                chip(best.durationLabel()), chip(best.goalContribution()),
                chip("Confidence " + (int) Math.round(best.confidence() * 100) + "%"));
        ActionButton start = ActionButton.primary("Start Focus Session");
        start.setOnAction(e -> nav.showDeepWork());
        card.getChildren().addAll(title, meta, start);
        return card;
    }

    /* ---------------- 3. why this action ---------------- */

    private Node whyThisAction(CoachRecommendation best) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");
        card.getChildren().add(section("WHY THIS ACTION?"));
        for (CoachReason r : best.reasons()) {
            Label l = new Label("• " + r.lens() + ": " + r.text() + (r.realData() ? "" : "  (limited data)"));
            l.getStyleClass().add(r.realData() ? "coach-why-line" : "text-muted");
            l.setWrapText(true);
            card.getChildren().add(l);
        }
        return card;
    }

    /* ---------------- 4 + 5. controls ---------------- */

    private Node controlsRow() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card");

        card.getChildren().add(section("IF I ONLY HAVE…"));
        HBox timeRow = new HBox(10);
        for (AvailableTimeSlot s : AvailableTimeSlot.values()) {
            ActionButton b = new ActionButton(s.label(), ActionButton.Variant.NEUTRAL);
            b.getStyleClass().add("deep-work-preset");
            if (coach.time() == s) b.getStyleClass().add("deep-work-preset-active");
            b.setOnAction(e -> { coach.setTime(s); render(); });
            timeRow.getChildren().add(b);
        }
        card.getChildren().add(timeRow);

        card.getChildren().add(section("ENERGY MODE"));
        HBox energyRow = new HBox(10);
        EnergyLevel[] levels = {EnergyLevel.LOW, EnergyLevel.MEDIUM, EnergyLevel.HIGH};
        String[] labels = {"Low Energy", "Normal Energy", "High Energy"};
        for (int i = 0; i < levels.length; i++) {
            EnergyLevel lvl = levels[i];
            ActionButton b = new ActionButton(labels[i], ActionButton.Variant.NEUTRAL);
            b.getStyleClass().add("deep-work-preset");
            if (coach.energy() == lvl) b.getStyleClass().add("deep-work-preset-active");
            b.setOnAction(e -> { coach.setEnergy(lvl); render(); });
            energyRow.getChildren().add(b);
        }
        card.getChildren().add(energyRow);
        return card;
    }

    /* ---------------- 6. alternatives ---------------- */

    private Node alternatives() {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.getChildren().add(section("ALTERNATIVES"));
        List<CoachRecommendation> alts = safe(coach::alternatives, List.of());
        if (alts.isEmpty()) {
            card.getChildren().add(label("No other active tasks right now.", "text-muted"));
            return card;
        }
        for (CoachRecommendation a : alts) {
            VBox row = new VBox(2);
            Label t = label(a.task().getTitle(), "text-body");
            t.setWrapText(true);
            Label r = label(a.priorityLabel() + " · " + a.deadlineLabel() + " · " + a.durationLabel(), "text-muted");
            row.getChildren().addAll(t, r);
            card.getChildren().add(row);
        }
        return card;
    }

    /* ---------------- 7. avoid now ---------------- */

    private Node avoidNow() {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.getChildren().add(section("AVOID NOW"));
        List<CoachAvoidItem> avoid = safe(coach::avoid, List.of());
        if (avoid.isEmpty()) {
            card.getChildren().add(label("Nothing to avoid — your active tasks fit your current context.", "text-muted"));
            return card;
        }
        for (CoachAvoidItem a : avoid) {
            VBox row = new VBox(2);
            Label t = label(a.title(), "text-body");
            t.setWrapText(true);
            Label r = label(a.reason(), "text-muted");
            r.setWrapText(true);
            row.getChildren().addAll(t, r);
            card.getChildren().add(row);
        }
        return card;
    }

    /* ---------------- 8. insights ---------------- */

    private Node insights(CoachContext ctx) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");
        card.getChildren().add(section("COACH INSIGHTS"));
        List<CoachInsight> list = safe(coach::insights, List.of());
        if (list.isEmpty()) {
            card.getChildren().add(label("No notable signals right now.", "text-muted"));
            return card;
        }
        for (CoachInsight i : list) {
            Label l = new Label("• " + i.message() + (i.realData() ? "" : "  (not enough data)"));
            l.getStyleClass().add(i.realData() ? "coach-why-line" : "text-muted");
            l.setWrapText(true);
            card.getChildren().add(l);
        }
        return card;
    }

    /* ---------------- helpers ---------------- */

    private Label section(String text) { return label(text, "coach-section-label"); }

    private VBox stat(String label, String value) {
        Label v = new Label(value);
        v.getStyleClass().add("kpi-compact-value");
        Label l = new Label(label.toUpperCase());
        l.getStyleClass().add("kpi-compact-label");
        VBox box = new VBox(2, v, l);
        box.setMinWidth(120);
        return box;
    }

    private Label chip(String text) { return label(text, "focus-meta-chip"); }

    private Label label(String text, String styleClass) {
        Label l = new Label(text);
        l.getStyleClass().add(styleClass);
        return l;
    }

    private <T> T safe(java.util.function.Supplier<T> supplier, T fallback) {
        try { return supplier.get(); } catch (Exception e) {
            System.err.println("[AiCoachView] coach read failed: " + e);
            return fallback;
        }
    }
}

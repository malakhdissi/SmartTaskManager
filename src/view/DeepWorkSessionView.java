package view;

import controller.NavigationController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import model.DayPeriod;
import model.EnergyLevel;
import model.Goal;
import model.Recommendation;
import model.Task;
import service.ServiceLocator;
import util.Formatter;
import view.components.ActionButton;

import java.time.LocalDateTime;

/**
 * DeepWorkSessionView — a Zen focus environment with full user control.
 *
 * <p>Top bar: exit to Dashboard, fullscreen toggle, light/dark theme toggle.
 * Setup: task-goal context, energy selector, duration presets, "today's deep
 * work" stat. Running: a calm, distraction-free timer with a live stats line.
 * End: a real session summary. The timer is correct and never fakes data; the
 * recommended task and goal context are pulled from real services.</p>
 */
public class DeepWorkSessionView {

    private enum Phase { SETUP, RUNNING, PAUSED }

    private final NavigationController nav;

    private Timeline ticker;
    private int plannedSeconds = 25 * 60;
    private int remainingSeconds = plannedSeconds;
    private int interruptions = 0;
    private Phase phase = Phase.SETUP;
    private EnergyLevel energy = EnergyLevel.MEDIUM;

    private final BorderPane root = new BorderPane();
    private final VBox col = new VBox(16);
    private Label timerLabel, statusLabel, liveStats;
    private ProgressBar bar;
    private VBox contextCard, statsCard;
    private HBox presetRow, energyRow;
    private ActionButton startBtn, pauseBtn, resumeBtn, restartBtn, endBtn;
    private final ActionButton[] presetButtons = new ActionButton[3];
    private final ActionButton[] energyButtons = new ActionButton[3];

    public DeepWorkSessionView(NavigationController nav) { this.nav = nav; }

    public Node build() {
        System.out.println("[DeepWork] rendering " + getClass().getName()
                + " — redesigned (top bar · energy selector · context card · theme/fullscreen)");
        root.getStyleClass().add("deep-work-pane");
        root.setTop(topBar());

        col.setAlignment(Pos.CENTER);

        Recommendation rec = safeBest();
        Label context = new Label(rec == null ? "Free focus block" : "Now focusing on · " + rec.getTask().getTitle());
        context.getStyleClass().add("deep-work-context");

        contextCard = taskGoalContextCard(rec);

        timerLabel = new Label(format(remainingSeconds));
        timerLabel.getStyleClass().add("deep-work-timer");

        bar = new ProgressBar(0);
        bar.setPrefWidth(380);

        statusLabel = new Label("Set your energy and duration, then press Start.");
        statusLabel.getStyleClass().add("text-muted");

        energy = defaultEnergy();
        energyRow = buildEnergyRow();
        presetRow = buildPresetRow();
        statsCard = buildStatsCard();
        liveStats = new Label("");
        liveStats.getStyleClass().add("text-muted");

        HBox controls = buildControls();

        col.getChildren().addAll(context, contextCard, timerLabel, bar, statusLabel,
                energyRow, presetRow, statsCard, liveStats, controls);
        root.setCenter(col);
        updateUi();
        return root;
    }

    /* ---------------- top control bar ---------------- */

    private HBox topBar() {
        HBox top = new HBox();
        top.getStyleClass().add("deep-work-topbar");
        top.setAlignment(Pos.CENTER_LEFT);

        ActionButton back = new ActionButton("← Exit to Dashboard", ActionButton.Variant.NEUTRAL);
        back.setOnAction(e -> exit());

        Region g = new Region(); HBox.setHgrow(g, Priority.ALWAYS);

        ActionButton fullscreen = ActionButton.ghost(isFullscreen() ? "Exit Fullscreen" : "Fullscreen");
        fullscreen.setOnAction(e -> {
            boolean now = !isFullscreen();
            if (nav.stage() != null) nav.stage().setFullScreen(now);
            fullscreen.setText(now ? "Exit Fullscreen" : "Fullscreen");
        });

        ActionButton theme = ActionButton.ghost(ThemeManager.isLight() ? "Dark theme" : "Light theme");
        theme.setOnAction(e -> {
            boolean light = ThemeManager.toggle(nav.scene());
            theme.setText(light ? "Dark theme" : "Light theme");
        });

        top.getChildren().addAll(back, g, fullscreen, theme);
        return top;
    }

    private boolean isFullscreen() { return nav.stage() != null && nav.stage().isFullScreen(); }

    private void exit() {
        if (ticker != null) ticker.stop();
        if (isFullscreen() && nav.stage() != null) nav.stage().setFullScreen(false);
        nav.showDashboard();
    }

    /* ---------------- task-goal context ---------------- */

    private VBox taskGoalContextCard(Recommendation rec) {
        VBox card = new VBox(6);
        card.getStyleClass().add("deep-work-card");
        card.setMaxWidth(420);
        Label heading = new Label("FOCUS CONTEXT");
        heading.getStyleClass().add("coach-section-label");
        card.getChildren().add(heading);

        if (rec == null) {
            Label none = new Label("No task selected — this is a free focus block.");
            none.getStyleClass().add("text-muted");
            none.setWrapText(true);
            card.getChildren().add(none);
            return card;
        }
        Task t = rec.getTask();
        Label title = new Label(t.getTitle());
        title.getStyleClass().add("text-body");
        title.setWrapText(true);

        HBox meta = new HBox(8);
        if (t.getPriority() != null) meta.getChildren().add(chip(t.getPriority().label() + " priority"));
        if (t.getDeadline() != null) meta.getChildren().add(chip("Due " + Formatter.date(t.getDeadline())));
        if (t.getEstimatedDuration() != null) meta.getChildren().add(chip(Formatter.duration(t.getEstimatedDuration())));

        Goal goal = safeActiveGoal();
        Label goalLine = new Label(goal == null
                ? "No active goal set."
                : "Contributes " + Math.round(t.getGoalContribution() * 100) + "% to: " + goal.getTitle());
        goalLine.getStyleClass().add("text-muted");
        goalLine.setWrapText(true);

        card.getChildren().addAll(title, meta, goalLine);
        return card;
    }

    /* ---------------- energy selector ---------------- */

    private HBox buildEnergyRow() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER);
        Label l = new Label("Energy");
        l.getStyleClass().add("text-muted");
        row.getChildren().add(l);
        EnergyLevel[] levels = {EnergyLevel.LOW, EnergyLevel.MEDIUM, EnergyLevel.HIGH};
        String[] labels = {"Low", "Normal", "High"};
        for (int i = 0; i < levels.length; i++) {
            EnergyLevel lvl = levels[i];
            ActionButton b = new ActionButton(labels[i], ActionButton.Variant.NEUTRAL);
            b.getStyleClass().add("deep-work-preset");
            b.setOnAction(e -> setEnergy(lvl));
            energyButtons[i] = b;
            row.getChildren().add(b);
        }
        highlightEnergy();
        return row;
    }

    private void setEnergy(EnergyLevel lvl) {
        if (phase != Phase.SETUP) return;
        energy = lvl;
        highlightEnergy();
    }

    private void highlightEnergy() {
        EnergyLevel[] levels = {EnergyLevel.LOW, EnergyLevel.MEDIUM, EnergyLevel.HIGH};
        for (int i = 0; i < energyButtons.length; i++) {
            energyButtons[i].getStyleClass().remove("deep-work-preset-active");
            if (levels[i] == energy) energyButtons[i].getStyleClass().add("deep-work-preset-active");
        }
    }

    private EnergyLevel defaultEnergy() {
        try {
            return ServiceLocator.temporalPlanningService().getProfile()
                    .energyFor(DayPeriod.of(LocalDateTime.now().toLocalTime()));
        } catch (Exception e) {
            return EnergyLevel.MEDIUM;
        }
    }

    /* ---------------- duration presets ---------------- */

    private HBox buildPresetRow() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER);
        int[] mins = {25, 50, 90};
        for (int i = 0; i < mins.length; i++) {
            int m = mins[i];
            ActionButton b = new ActionButton(m + " min", ActionButton.Variant.NEUTRAL);
            b.getStyleClass().add("deep-work-preset");
            b.setOnAction(e -> setPlanned(m));
            presetButtons[i] = b;
            row.getChildren().add(b);
        }
        Spinner<Integer> custom = new Spinner<>(5, 180, 30, 5);
        custom.setEditable(true);
        custom.setPrefWidth(110);
        ActionButton apply = ActionButton.ghost("Set custom");
        apply.setOnAction(e -> setPlanned(custom.getValue() == null ? 30 : custom.getValue()));
        row.getChildren().addAll(custom, apply);
        return row;
    }

    private void setPlanned(int minutes) {
        if (phase != Phase.SETUP) return;
        plannedSeconds = minutes * 60;
        remainingSeconds = plannedSeconds;
        timerLabel.setText(format(remainingSeconds));
        bar.setProgress(0);
        statusLabel.setText(minutes + "-minute block ready. Press Start when you are.");
        for (int i = 0; i < presetButtons.length; i++) {
            boolean active = (minutes == 25 && i == 0) || (minutes == 50 && i == 1) || (minutes == 90 && i == 2);
            presetButtons[i].getStyleClass().remove("deep-work-preset-active");
            if (active) presetButtons[i].getStyleClass().add("deep-work-preset-active");
        }
    }

    /* ---------------- session statistics (setup) ---------------- */

    private VBox buildStatsCard() {
        VBox card = new VBox(6);
        card.getStyleClass().add("deep-work-card");
        card.setMaxWidth(420);
        Label heading = new Label("YOUR DEEP WORK");
        heading.getStyleClass().add("coach-section-label");
        card.getChildren().add(heading);
        try {
            var snap = ServiceLocator.analyticsService().snapshot();
            card.getChildren().add(statRow("Completed deep-work logged", hours(snap.deepWorkMinutes())));
            card.getChildren().add(statRow("Tasks completed", String.valueOf(snap.completed())));
        } catch (Exception e) {
            card.getChildren().add(new Label("No deep-work data yet.") {{ getStyleClass().add("text-muted"); }});
        }
        return card;
    }

    /* ---------------- controls ---------------- */

    private HBox buildControls() {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER);
        startBtn = ActionButton.primary("Start");
        startBtn.setOnAction(e -> start());
        pauseBtn = ActionButton.ghost("Pause");
        pauseBtn.setOnAction(e -> pause());
        resumeBtn = ActionButton.ghost("Resume");
        resumeBtn.setOnAction(e -> resume());
        restartBtn = new ActionButton("Restart", ActionButton.Variant.NEUTRAL);
        restartBtn.setOnAction(e -> restart());
        endBtn = new ActionButton("Finish", ActionButton.Variant.NEUTRAL);
        endBtn.setOnAction(e -> end(false));
        row.getChildren().addAll(startBtn, pauseBtn, resumeBtn, restartBtn, endBtn);
        return row;
    }

    private void start() {
        if (phase == Phase.RUNNING) return;
        phase = Phase.RUNNING;
        statusLabel.setText("Focusing… (" + energy.label() + " energy)");
        startTicker();
        updateUi();
    }

    private void pause() {
        if (phase != Phase.RUNNING) return;
        if (ticker != null) ticker.pause();
        interruptions++;
        phase = Phase.PAUSED;
        statusLabel.setText("Paused (" + interruptions + " interruption" + (interruptions == 1 ? "" : "s") + ").");
        updateUi();
    }

    private void resume() {
        if (phase != Phase.PAUSED) return;
        phase = Phase.RUNNING;
        statusLabel.setText("Focusing…");
        if (ticker != null) ticker.play();
        updateUi();
    }

    private void restart() {
        if (ticker != null) ticker.stop();
        remainingSeconds = plannedSeconds;
        interruptions = 0;
        timerLabel.setText(format(remainingSeconds));
        bar.setProgress(0);
        phase = Phase.RUNNING;
        statusLabel.setText("Restarted. Focusing…");
        startTicker();
        updateUi();
    }

    private void startTicker() {
        if (ticker != null) ticker.stop();
        ticker = new Timeline(new KeyFrame(Duration.seconds(1), e -> tick()));
        ticker.setCycleCount(Timeline.INDEFINITE);
        ticker.play();
    }

    private void tick() {
        remainingSeconds = Math.max(0, remainingSeconds - 1);
        timerLabel.setText(format(remainingSeconds));
        bar.setProgress(plannedSeconds == 0 ? 0 : 1d - remainingSeconds / (double) plannedSeconds);
        liveStats.setText("Remaining " + format(remainingSeconds) + "  ·  Interruptions " + interruptions);
        if (remainingSeconds == 0) end(true);
    }

    private void end(boolean completed) {
        if (ticker != null) ticker.stop();
        showSummary(completed, plannedSeconds - remainingSeconds);
    }

    private void updateUi() {
        boolean setup = phase == Phase.SETUP;
        // Setup-only panels keep the session distraction-free once running.
        setShown(contextCard, setup);
        setShown(energyRow, setup);
        setShown(presetRow, setup);
        setShown(statsCard, setup);
        setShown(liveStats, !setup);

        startBtn.setDisable(phase == Phase.RUNNING);
        pauseBtn.setDisable(phase != Phase.RUNNING);
        resumeBtn.setDisable(phase != Phase.PAUSED);
        restartBtn.setDisable(setup);
        endBtn.setDisable(setup);
    }

    private static void setShown(Node n, boolean shown) {
        n.setVisible(shown);
        n.setManaged(shown);
    }

    /* ---------------- summary ---------------- */

    private void showSummary(boolean completed, int focusedSeconds) {
        col.getChildren().clear();
        Label title = new Label(completed ? "Deep Work block complete" : "Session ended");
        title.getStyleClass().add("deep-work-context");

        VBox summary = new VBox(8);
        summary.getStyleClass().add("deep-work-card");
        summary.setMaxWidth(420);
        summary.getChildren().addAll(
                statRow("Planned", plannedSeconds / 60 + " min"),
                statRow("Focused", focusedSeconds / 60 + " min"),
                statRow("Interruptions", String.valueOf(interruptions)),
                statRow("Energy", energy.label()));

        HBox actions = new HBox(12);
        actions.setAlignment(Pos.CENTER);
        ActionButton again = ActionButton.ghost("Start another");
        again.setOnAction(e -> nav.showDeepWork());
        ActionButton dash = ActionButton.primary("Back to Dashboard");
        dash.setOnAction(e -> exit());
        actions.getChildren().addAll(again, dash);

        col.getChildren().addAll(title, summary, actions);
    }

    /* ---------------- helpers ---------------- */

    private HBox statRow(String label, String value) {
        Label l = new Label(label); l.getStyleClass().add("text-muted"); l.setMinWidth(180);
        Label v = new Label(value); v.getStyleClass().add("text-body");
        return new HBox(10, l, v);
    }

    private Label chip(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("focus-meta-chip");
        return l;
    }

    private Recommendation safeBest() {
        try { return ServiceLocator.recommendationService().getBestNext(); } catch (Exception e) { return null; }
    }

    private Goal safeActiveGoal() {
        try { return ServiceLocator.goalService().getActive(); } catch (Exception e) { return null; }
    }

    private static String hours(long minutes) {
        if (minutes <= 0) return "0h";
        double h = minutes / 60.0;
        return h == Math.floor(h) ? (long) h + "h" : String.format(java.util.Locale.US, "%.1fh", h);
    }

    private String format(int s) { return String.format("%02d:%02d", s / 60, s % 60); }
}

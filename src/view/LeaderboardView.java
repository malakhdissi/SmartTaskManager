package view;

import controller.NavigationController;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.User;
import service.AnalyticsService;
import service.ServiceLocator;
import view.components.ScreenTitle;

/**
 * LeaderboardView — shows the current user's REAL standing only.
 *
 * <p>A multi-user competitive league needs other real users, which don't exist
 * in this single-user build. Per the data-honesty rule we do not fabricate
 * competitors: the league table is disabled with a clear message, and the
 * "Your standing" card is computed entirely from real task data.</p>
 */
public class LeaderboardView {

    private final NavigationController nav;
    public LeaderboardView(NavigationController nav) { this.nav = nav; }

    public Node build() {
        VBox root = new VBox(16);
        root.getChildren().add(new ScreenTitle("Productivity League",
                "Your standing, computed from your real activity."));

        root.getChildren().add(yourStanding());

        // League table is genuinely unavailable — say so, don't fake competitors.
        VBox disabled = new VBox(6);
        disabled.getStyleClass().add("card");
        Label t = new Label("Multiplayer league — not available yet");
        t.getStyleClass().add("text-body");
        Label d = new Label("Competitive rankings need other real players. We won't show sample competitors; "
                + "the league unlocks when multi-user support ships.");
        d.getStyleClass().add("text-muted");
        d.setWrapText(true);
        disabled.getChildren().addAll(t, d);
        root.getChildren().add(disabled);
        return root;
    }

    private Node yourStanding() {
        VBox card = new VBox(12);
        card.getStyleClass().addAll("leader-card", "leader-card-you");

        User user = ServiceLocator.userService().getCurrentUser().orElse(null);
        AnalyticsService.Snapshot snap = safeSnapshot();
        long completed = snap.completed();
        int score = snap.completionRate();
        int streak = user == null ? 0 : user.getCurrentStreakDays();

        HBox top = new HBox(16);
        top.setAlignment(Pos.CENTER_LEFT);
        Label name = new Label((user == null ? "You" : user.getDisplayName()) + "  (you)");
        name.getStyleClass().add("leader-name");
        Label division = new Label(division(completed));
        division.getStyleClass().add("leader-division");
        Region g = new Region(); HBox.setHgrow(g, Priority.ALWAYS);
        Label level = new Label("Level " + level(completed));
        level.getStyleClass().add("leader-stat-value");
        top.getChildren().addAll(name, division, g, level);

        HBox stats = new HBox(28);
        stats.getChildren().addAll(
                stat("Focus score", score + "%"),
                stat("Deep work", hours(snap.deepWorkMinutes())),
                stat("Completed", String.valueOf(completed)),
                stat("Streak", streak + "d"));

        card.getChildren().addAll(top, stats);
        return card;
    }

    private VBox stat(String label, String value) {
        Label v = new Label(value);
        v.getStyleClass().add("leader-stat-value");
        Label l = new Label(label.toUpperCase());
        l.getStyleClass().add("leader-stat-label");
        VBox box = new VBox(2, v, l);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private AnalyticsService.Snapshot safeSnapshot() {
        try { return ServiceLocator.analyticsService().snapshot(); }
        catch (Exception e) { return new AnalyticsService.Snapshot(0, 0, 0, 0, 0, 0, false); }
    }

    private static String division(long completed) {
        if (completed >= 50) return "Diamond";
        if (completed >= 25) return "Gold";
        if (completed >= 10) return "Silver";
        return "Bronze";
    }

    private static int level(long completed) { return (int) (completed / 5) + 1; }

    private static String hours(long minutes) {
        if (minutes <= 0) return "0h";
        double h = minutes / 60.0;
        return h == Math.floor(h) ? (long) h + "h" : String.format(java.util.Locale.US, "%.1fh", h);
    }
}

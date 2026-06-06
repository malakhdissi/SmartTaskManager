package view.components;

import controller.NavigationController;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Sidebar — vertical navigation rail used on every main screen
 * (except Welcome / Login / Deep Work).
 *
 * <p>Centralizes the list of screens so adding a future screen requires
 * a single line here — not a search across the codebase.</p>
 */
public class Sidebar extends VBox {

    private final NavigationController nav;

    /** The screen key currently active (used to highlight the matching row). */
    private String activeKey;

    public Sidebar(NavigationController nav, String activeKey) {
        this.nav = nav;
        this.activeKey = activeKey;
        getStyleClass().add("sidebar");
        build();
    }

    private void build() {
        getChildren().clear();

        BrandMark brand = new BrandMark(BrandMark.Size.COMPACT);
        brand.getStyleClass().add("sidebar-logo");
        getChildren().add(brand);

        section("WORK");
        item("dashboard",        "Dashboard",             nav::showDashboard);
        item("tasks",            "Tasks",                 nav::showTaskList);
        item("deep-work",        "Deep Work",             nav::showDeepWork);

        section("INTELLIGENCE");
        item("ai-coach",         "AI Coach",              nav::showAiCoach);
        item("smart-schedule",   "Schedule",              nav::showSmartSchedule);
        item("temporal",         "Temporal Intelligence", nav::showTemporal);
        item("insights",         "Insights",              nav::showInsights);

        section("PERSONAL");
        item("goals",            "Goals",                 nav::showGoals);
        item("habits",           "Habits",                nav::showHabits);
        item("settings",         "Profile",               nav::showSettings);

        section("EXPERIMENTAL");
        item("leaderboard",      "Leaderboard",           nav::showLeaderboard);
        item("distractions",     "Distraction Management", nav::showDistractions);
        // Kept reachable (not in the primary groups) so no screen is orphaned:
        item("recommendations",  "Recommendations",       nav::showRecommendations);
        item("persona",          "Persona",               nav::showPersona);
        item("timeline",         "Productivity Timeline",  nav::showTimeline);
    }

    private void section(String label) {
        Label l = new Label(label);
        l.getStyleClass().add("sidebar-section-label");
        getChildren().add(l);
    }

    private void item(String key, String label, Runnable action) {
        Button b = new Button(label);
        b.getStyleClass().add("sidebar-item");
        if (key.equals(activeKey)) b.getStyleClass().add("sidebar-item-active");
        b.setMaxWidth(Double.MAX_VALUE);
        b.setOnAction(e -> action.run());
        b.setFocusTraversable(false);
        getChildren().add(b);
    }

    /** Adds a flexible region so the sidebar fills the full height. */
    public Sidebar withFiller() {
        Region filler = new Region();
        VBox.setVgrow(filler, javafx.scene.layout.Priority.ALWAYS);
        getChildren().add(filler);
        return this;
    }
}

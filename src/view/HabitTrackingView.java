package view;

import controller.NavigationController;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import view.components.EmptyState;
import view.components.ScreenTitle;

/**
 * HabitTrackingView — habit tracking is not yet backed by real persistence
 * (it ships in a later phase). Rather than show fabricated streaks, this screen
 * shows an honest, professional empty state.
 */
public class HabitTrackingView {

    private final NavigationController nav;
    public HabitTrackingView(NavigationController nav) { this.nav = nav; }

    public Node build() {
        VBox root = new VBox(16);
        root.getChildren().add(new ScreenTitle("Habits",
                "Build consistency over time — progress, never punishment for missed days."));

        root.getChildren().add(new EmptyState(
                "Habit tracking is coming soon",
                "We don't show sample streaks here. Real habit tracking — create habits, log completions, "
                        + "and see your consistency — arrives in an upcoming update."));
        return root;
    }
}

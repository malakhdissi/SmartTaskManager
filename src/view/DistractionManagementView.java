package view;

import controller.NavigationController;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import view.components.EmptyState;
import view.components.ScreenTitle;

/**
 * DistractionManagementView — distraction tracking is not yet backed by real
 * data (manual logging + persistence ships in a later phase). Instead of
 * showing fabricated usage minutes, this screen shows an honest empty state.
 */
public class DistractionManagementView {

    private final NavigationController nav;
    public DistractionManagementView(NavigationController nav) { this.nav = nav; }

    public Node build() {
        VBox root = new VBox(16);
        root.getChildren().add(new ScreenTitle("Distraction Management",
                "Direction matters more than perfection. Tiny reductions add up."));

        root.getChildren().add(new EmptyState(
                "Distraction tracking is coming soon",
                "We won't show sample usage numbers. Real tracking — log distraction time, set reduction "
                        + "goals, and see weekly trends — arrives in an upcoming update."));
        return root;
    }
}

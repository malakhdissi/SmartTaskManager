package view;

import controller.NavigationController;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import view.components.ActionButton;
import view.components.ScreenTitle;

/**
 * PersonaAdaptationView — switches the active persona.
 *
 * <p>Personas use neutral, non-medical labels in line with the product's
 * ethics: Anti-Procrastination, Anti-Scrolling, Deep Work, Short Focus,
 * Balanced. Each card describes how the product adapts.</p>
 */
public class PersonaAdaptationView {

    private final NavigationController nav;
    public PersonaAdaptationView(NavigationController nav) { this.nav = nav; }

    public Node build() {
        VBox root = new VBox(16);
        ScreenTitle title = new ScreenTitle("Persona",
                "Pick how the product should adapt to you today. You can change at any time.");
        title.addAction(new view.components.PreviewBadge());
        root.getChildren().add(title);

        FlowPane grid = new FlowPane(16, 16);
        grid.getChildren().addAll(
                personaCard("Anti-Procrastination",
                        "Shorter starting blocks. Gentle nudges on stuck tasks. The product reduces friction to begin.",
                        false),
                personaCard("Anti-Scrolling",
                        "More attention to distraction sources. Calmer dashboard with one prominent focus action.",
                        false),
                personaCard("Deep Work",
                        "Default 90-minute uninterrupted blocks. Quiet UI. Aggressive notification muting.",
                        false),
                personaCard("Short Focus",
                        "Default 25-minute Pomodoro blocks. Frequent micro-rewards for starting.",
                        false),
                personaCard("Balanced",
                        "A mix tuned across all signals. Good default while you explore.",
                        false)
        );
        root.getChildren().add(grid);
        return root;
    }

    private Node personaCard(String name, String description, boolean active) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card-elevated");
        card.setMinWidth(260);
        HBox.setHgrow(card, Priority.ALWAYS);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label t = new Label(name);
        t.getStyleClass().add("text-body");
        Label tag = new Label(active ? "Active" : "Available");
        tag.getStyleClass().addAll("tag", active ? "tag-success" : "tag-muted");
        header.getChildren().addAll(t, tag);

        Label d = new Label(description);
        d.getStyleClass().add("text-muted");
        d.setWrapText(true);

        ActionButton activate = ActionButton.ghost(active ? "Currently active" : "Activate");
        activate.setDisable(active);
        activate.setOnAction(e -> nav.notifySuccess("Persona switched to: " + name));

        card.getChildren().addAll(header, d, activate);
        return card;
    }
}

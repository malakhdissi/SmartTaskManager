package view;

import controller.AuthUiController;
import controller.NavigationController;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import service.ServiceLocator;
import util.Constants;
import view.components.ActionButton;
import view.components.BrandMark;

/**
 * WelcomeView — full-bleed split landing screen.
 *
 * <p>Left: brand + value proposition (one tagline, three concrete benefits).
 * Right: a focused action panel with two distinct CTAs that go to genuinely
 * different places — Get Started (sign up) and Log in. The old centered card
 * with a placeholder illustration and an "MVP" badge is gone.</p>
 */
public class WelcomeView extends StackPane {

    public WelcomeView(NavigationController nav) {
        getStyleClass().add("welcome-pane");

        HBox split = new HBox();
        split.getStyleClass().add("entry-split");
        split.getChildren().addAll(brandPanel(), actionPanel(nav));
        getChildren().add(split);
    }

    /* ---------------- left: brand + value proposition ---------------- */
    private VBox brandPanel() {
        VBox panel = new VBox(22);
        panel.getStyleClass().add("brand-panel");
        panel.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(panel, Priority.ALWAYS);

        BrandMark brand = new BrandMark(BrandMark.Size.LARGE);

        Label tagline = new Label(Constants.APP_TAGLINE);
        tagline.getStyleClass().add("welcome-tagline-lg");
        tagline.setWrapText(true);

        VBox bullets = new VBox(12);
        bullets.getChildren().addAll(
                bullet("See your single best next action — instantly."),
                bullet("Protect Deep Work from distraction."),
                bullet("Build momentum with calm, honest progress."));

        panel.getChildren().addAll(brand, tagline, bullets);
        return panel;
    }

    private HBox bullet(String text) {
        Label dot = new Label("•");
        dot.getStyleClass().add("welcome-bullet-dot");
        Label t = new Label(text);
        t.getStyleClass().add("welcome-bullet-text");
        t.setWrapText(true);
        HBox row = new HBox(10, dot, t);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    /* ---------------- right: focused actions ---------------- */
    private VBox actionPanel(NavigationController nav) {
        VBox panel = new VBox(16);
        panel.getStyleClass().add("action-panel");
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setMaxWidth(420);
        HBox.setHgrow(panel, Priority.ALWAYS);

        Label heading = new Label("Take control of your day.");
        heading.getStyleClass().add("text-display");
        heading.setWrapText(true);

        Label sub = new Label("Create a free account in seconds, or sign in to pick up where you left off.");
        sub.getStyleClass().add("text-subtitle");
        sub.setWrapText(true);

        ActionButton getStarted = ActionButton.primary("Get Started");
        getStarted.setMaxWidth(Double.MAX_VALUE);
        getStarted.setOnAction(e -> nav.showSignup());

        ActionButton login = ActionButton.ghost("Log in");
        login.setMaxWidth(Double.MAX_VALUE);
        login.setOnAction(e -> nav.showLogin());

        ActionButton guest = ActionButton.neutral("Continue as guest");
        guest.setMaxWidth(Double.MAX_VALUE);
        guest.setOnAction(e -> new AuthUiController(nav).guest());

        panel.getChildren().addAll(heading, sub, getStarted, login, guest);

        if (!ServiceLocator.isPersistenceOnline()) {
            Label offline = new Label("Offline mode — accounts and tasks won't be saved after you close the app.");
            offline.getStyleClass().add("offline-note");
            offline.setWrapText(true);
            panel.getChildren().add(offline);
        }
        return panel;
    }
}

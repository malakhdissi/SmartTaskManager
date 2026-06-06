package view;

import controller.NavigationController;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import util.Constants;
import view.components.ActionButton;
import view.components.BrandMark;

/**
 * ForgotPasswordView — honest placeholder for password recovery.
 *
 * <p>Email-based reset isn't built yet. Rather than pretend, we say so clearly
 * and professionally, and give the user a real next step (create an account).
 * Wired into the entry split-panel layout for visual consistency.</p>
 */
public class ForgotPasswordView extends StackPane {

    public ForgotPasswordView(NavigationController nav) {
        getStyleClass().add("welcome-pane");

        HBox split = new HBox();
        split.getStyleClass().add("entry-split");
        split.getChildren().addAll(brandPanel(), formPanel(nav));
        getChildren().add(split);
    }

    private VBox brandPanel() {
        VBox panel = new VBox(18);
        panel.getStyleClass().add("brand-panel");
        panel.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(panel, Priority.ALWAYS);

        Label tagline = new Label(Constants.APP_TAGLINE);
        tagline.getStyleClass().add("welcome-tagline-lg");
        tagline.setWrapText(true);

        panel.getChildren().addAll(new BrandMark(BrandMark.Size.LARGE), tagline);
        return panel;
    }

    private VBox formPanel(NavigationController nav) {
        VBox wrap = new VBox();
        wrap.getStyleClass().add("action-panel");
        wrap.setAlignment(Pos.CENTER);
        HBox.setHgrow(wrap, Priority.ALWAYS);

        VBox card = new VBox(14);
        card.getStyleClass().add("auth-card");

        Label title = new Label("Reset your password");
        title.getStyleClass().add("text-title");

        Label message = new Label(
                "Secure, email-based password recovery is coming soon. It isn't available yet, "
                + "so we can't send a reset link at the moment.\n\n"
                + "For now, you can create a new account to keep moving — or reach out to support "
                + "if you need help with an existing one.");
        message.getStyleClass().add("text-subtitle");
        message.setWrapText(true);

        ActionButton createAccount = ActionButton.primary("Create a new account");
        createAccount.setMaxWidth(Double.MAX_VALUE);
        createAccount.setOnAction(e -> nav.showSignup());

        Hyperlink back = new Hyperlink("← Back to login");
        back.getStyleClass().add("auth-link");
        back.setOnAction(e -> nav.showLogin());

        card.getChildren().addAll(title, message, createAccount, back);
        wrap.getChildren().add(card);
        return wrap;
    }
}

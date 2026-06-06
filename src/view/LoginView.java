package view;

import controller.AuthUiController;
import controller.NavigationController;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import util.Constants;
import view.components.ActionButton;
import view.components.BrandMark;
import view.components.PasswordBox;

/**
 * LoginView — real authentication against the service/DAO layer.
 *
 * <p>No demo wording, no pre-filled credentials. Validation failures and bad
 * credentials produce precise, inline messages from {@link service.AuthResult}
 * (no account found / incorrect password / fill both fields).</p>
 */
public class LoginView extends StackPane {

    public LoginView(NavigationController nav) {
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

        VBox card = new VBox(12);
        card.getStyleClass().add("auth-card");

        Label title = new Label("Welcome back");
        title.getStyleClass().add("text-title");
        Label sub = new Label("Sign in to continue to " + Constants.APP_NAME);
        sub.getStyleClass().add("text-subtitle");

        TextField email = new TextField();
        email.setPromptText("Email");

        PasswordBox password = new PasswordBox("Password");

        Label error = new Label();
        error.getStyleClass().add("auth-error");
        error.setWrapText(true);
        error.setManaged(false);
        error.setVisible(false);

        AuthUiController auth = new AuthUiController(nav);
        Runnable submit = () -> {
            clearError(error);
            auth.login(email.getText(), password.getChars(), r -> showError(error, r.message()));
        };

        ActionButton signIn = ActionButton.primary("Sign in");
        signIn.setMaxWidth(Double.MAX_VALUE);
        signIn.setDefaultButton(true);
        signIn.setOnAction(e -> submit.run());
        email.setOnAction(e -> submit.run());
        password.setOnEnter(submit);

        Hyperlink forgot = new Hyperlink("Forgot password?");
        forgot.getStyleClass().add("auth-link");
        forgot.setOnAction(e -> nav.showForgotPassword());

        HBox signupRow = new HBox(6);
        signupRow.setAlignment(Pos.CENTER_LEFT);
        Label prompt = new Label("New here?");
        prompt.getStyleClass().add("text-muted");
        Hyperlink toSignup = new Hyperlink("Create an account");
        toSignup.getStyleClass().add("auth-link");
        toSignup.setOnAction(e -> nav.showSignup());
        Region g = new Region();
        HBox.setHgrow(g, Priority.ALWAYS);
        signupRow.getChildren().addAll(prompt, toSignup, g, forgot);

        Hyperlink back = new Hyperlink("← Back to welcome");
        back.getStyleClass().add("auth-link");
        back.setOnAction(e -> nav.showWelcome());

        card.getChildren().addAll(title, sub, email, password, error, signIn, signupRow, back);
        wrap.getChildren().add(card);
        return wrap;
    }

    private void showError(Label error, String message) {
        error.setText(message);
        error.setManaged(true);
        error.setVisible(true);
    }

    private void clearError(Label error) {
        error.setText("");
        error.setManaged(false);
        error.setVisible(false);
    }
}

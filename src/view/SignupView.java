package view;

import controller.AuthUiController;
import controller.NavigationController;
import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import util.Constants;
import util.PasswordStrength;
import view.components.ActionButton;
import view.components.BrandMark;
import view.components.PasswordBox;

/**
 * SignupView — real account creation with inline validation and a live
 * password-strength meter.
 *
 * <p>Validation and uniqueness are enforced by {@link service.AuthService};
 * this view surfaces the resulting message inline under the form and shows
 * advisory strength feedback as the user types.</p>
 */
public class SignupView extends StackPane {

    public SignupView(NavigationController nav) {
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

        Label sub = new Label("Start free. Your tasks, goals, and progress stay private to your account.");
        sub.getStyleClass().add("text-subtitle");
        sub.setWrapText(true);

        panel.getChildren().addAll(new BrandMark(BrandMark.Size.LARGE), tagline, sub);
        return panel;
    }

    private VBox formPanel(NavigationController nav) {
        VBox wrap = new VBox();
        wrap.getStyleClass().add("action-panel");
        wrap.setAlignment(Pos.CENTER);
        HBox.setHgrow(wrap, Priority.ALWAYS);

        VBox card = new VBox(12);
        card.getStyleClass().add("auth-card");

        Label title = new Label("Create your account");
        title.getStyleClass().add("text-title");
        Label sub = new Label("It takes less than a minute.");
        sub.getStyleClass().add("text-subtitle");

        TextField name = new TextField();
        name.setPromptText("Your name");

        TextField email = new TextField();
        email.setPromptText("Email");

        PasswordBox password = new PasswordBox("Password (8+ chars, letters & numbers)");

        Label strength = new Label();
        strength.getStyleClass().add("password-strength");
        strength.setManaged(false);
        strength.setVisible(false);
        password.onTextChanged(() -> updateStrength(password, strength));

        Label error = new Label();
        error.getStyleClass().add("auth-error");
        error.setWrapText(true);
        error.setManaged(false);
        error.setVisible(false);

        AuthUiController auth = new AuthUiController(nav);
        Runnable submit = () -> {
            clearError(error);
            auth.signup(name.getText(), email.getText(), password.getChars(),
                    r -> showError(error, r.message()));
        };

        ActionButton create = ActionButton.primary("Create account");
        create.setMaxWidth(Double.MAX_VALUE);
        create.setDefaultButton(true);
        create.setOnAction(e -> submit.run());
        name.setOnAction(e -> submit.run());
        email.setOnAction(e -> submit.run());
        password.setOnEnter(submit);

        HBox loginRow = new HBox(6);
        loginRow.setAlignment(Pos.CENTER_LEFT);
        Label prompt = new Label("Already have an account?");
        prompt.getStyleClass().add("text-muted");
        Hyperlink toLogin = new Hyperlink("Log in");
        toLogin.getStyleClass().add("auth-link");
        toLogin.setOnAction(e -> nav.showLogin());
        loginRow.getChildren().addAll(prompt, toLogin);

        Hyperlink back = new Hyperlink("← Back to welcome");
        back.getStyleClass().add("auth-link");
        back.setOnAction(e -> nav.showWelcome());

        card.getChildren().addAll(title, sub, name, email, password, strength, error, create, loginRow, back);
        wrap.getChildren().add(card);
        return wrap;
    }

    private void updateStrength(PasswordBox password, Label strength) {
        char[] pw = password.getChars();
        if (pw.length == 0) {
            strength.setManaged(false);
            strength.setVisible(false);
            return;
        }
        PasswordStrength.Level level = PasswordStrength.level(pw);
        strength.setText("Password strength: " + label(level));
        strength.getStyleClass().removeAll("strength-weak", "strength-fair", "strength-strong");
        strength.getStyleClass().add(switch (level) {
            case WEAK -> "strength-weak";
            case FAIR -> "strength-fair";
            case STRONG -> "strength-strong";
        });
        strength.setManaged(true);
        strength.setVisible(true);
    }

    private static String label(PasswordStrength.Level level) {
        return switch (level) {
            case WEAK -> "Weak";
            case FAIR -> "Fair";
            case STRONG -> "Strong";
        };
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

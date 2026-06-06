package view.components;

import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;

/**
 * PasswordBox — a password input with a Show/Hide toggle.
 *
 * <p>JavaFX has no built-in reveal, so this overlays a {@link PasswordField}
 * (masked) and a {@link TextField} (plain) whose text properties are bound
 * bidirectionally; the toggle swaps which one is visible. Callers read the
 * value via {@link #getChars()} and never touch the underlying controls.</p>
 */
public class PasswordBox extends HBox {

    private final PasswordField hidden = new PasswordField();
    private final TextField shown = new TextField();
    private boolean revealed = false;

    public PasswordBox(String prompt) {
        setSpacing(8);
        setAlignment(Pos.CENTER_LEFT);

        hidden.setPromptText(prompt);
        shown.setPromptText(prompt);
        // Keep both in sync; only one is ever visible.
        shown.textProperty().bindBidirectional(hidden.textProperty());
        shown.setManaged(false);
        shown.setVisible(false);

        StackPane field = new StackPane(hidden, shown);
        field.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(field, Priority.ALWAYS);

        Hyperlink toggle = new Hyperlink("Show");
        toggle.getStyleClass().add("auth-link");
        toggle.setFocusTraversable(false);
        toggle.setOnAction(e -> {
            revealed = !revealed;
            shown.setVisible(revealed);
            shown.setManaged(revealed);
            hidden.setVisible(!revealed);
            hidden.setManaged(!revealed);
            toggle.setText(revealed ? "Hide" : "Show");
            TextField active = revealed ? shown : hidden;
            active.requestFocus();
            active.positionCaret(getText().length());
        });

        getChildren().addAll(field, toggle);
    }

    /** The current password value (never null). */
    public String getText() {
        return hidden.getText() == null ? "" : hidden.getText();
    }

    /** The password as a char array for hashing. */
    public char[] getChars() {
        return getText().toCharArray();
    }

    /** Runs {@code action} when the user presses Enter in either field. */
    public void setOnEnter(Runnable action) {
        hidden.setOnAction(e -> action.run());
        shown.setOnAction(e -> action.run());
    }

    /** Observe text changes (for the live strength meter). */
    public void onTextChanged(Runnable action) {
        hidden.textProperty().addListener((o, a, b) -> action.run());
    }
}

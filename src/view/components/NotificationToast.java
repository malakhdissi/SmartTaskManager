package view.components;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * NotificationToast — non-blocking, fade-out feedback overlay.
 *
 * <p>Mounted on the root StackPane by {@link controller.NavigationController}.
 * Stays visible for 2.5 seconds, then fades out. We choose colored borders
 * (success / warning / danger / primary) instead of red backgrounds — calm
 * over alarming.</p>
 */
public class NotificationToast {

    public enum Kind { PRIMARY, SUCCESS, WARNING, DANGER }

    /**
     * Shows the toast inside the supplied root container.
     * @param root  the StackPane that hosts the whole scene
     * @param text  the message to show
     * @param kind  visual tone
     */
    public static void show(StackPane root, String text, Kind kind) {
        Platform.runLater(() -> {
            Label toast = new Label(text);
            toast.getStyleClass().add("toast");
            switch (kind) {
                case SUCCESS -> toast.getStyleClass().add("toast-success");
                case WARNING -> toast.getStyleClass().add("toast-warning");
                case DANGER  -> toast.getStyleClass().add("toast-danger");
                case PRIMARY -> { /* default border */ }
            }
            StackPane.setAlignment(toast, Pos.BOTTOM_CENTER);
            toast.setTranslateY(-32);
            root.getChildren().add(toast);

            PauseTransition wait = new PauseTransition(Duration.millis(2200));
            FadeTransition fade  = new FadeTransition(Duration.millis(400), toast);
            fade.setFromValue(1); fade.setToValue(0);
            SequentialTransition seq = new SequentialTransition(wait, fade);
            seq.setOnFinished(e -> root.getChildren().remove(toast));
            seq.play();
        });
    }
}

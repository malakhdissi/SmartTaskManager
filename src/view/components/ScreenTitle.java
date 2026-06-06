package view.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * ScreenTitle — reusable page heading with title + subtitle + optional right-side action area.
 *
 * <p>Why a dedicated component: every main screen has a heading. Centralizing
 * the typography + spacing means we never get drift between screens.</p>
 */
public class ScreenTitle extends HBox {

    private final HBox rightSlot = new HBox(8);

    /** Standard title + subtitle. */
    public ScreenTitle(String title, String subtitle) {
        setSpacing(16);
        setAlignment(Pos.CENTER_LEFT);

        VBox texts = new VBox(4);
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("text-title");
        texts.getChildren().add(titleLabel);

        if (subtitle != null && !subtitle.isBlank()) {
            Label sub = new Label(subtitle);
            sub.getStyleClass().add("text-subtitle");
            texts.getChildren().add(sub);
        }

        // Pushes the right slot to the far edge.
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        rightSlot.setAlignment(Pos.CENTER_RIGHT);
        getChildren().addAll(texts, spacer, rightSlot);
    }

    /** Adds an action node (typically a button) to the right side. */
    public ScreenTitle addAction(javafx.scene.Node node) {
        rightSlot.getChildren().add(node);
        return this;
    }
}

package view.components;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * EmptyState — calm, non-judgmental empty-data view.
 *
 * <p>We never leave a blank rectangle. Used in Task List, Insights, Focus
 * sessions, etc. Optional action button below the message.</p>
 */
public class EmptyState extends VBox {

    public EmptyState(String title, String hint, Node action) {
        getStyleClass().add("empty-state");
        setAlignment(Pos.CENTER);
        setSpacing(6);

        Label t = new Label(title);
        t.getStyleClass().add("empty-state-title");

        Label h = new Label(hint);
        h.getStyleClass().add("empty-state-hint");
        h.setWrapText(true);

        getChildren().addAll(t, h);

        if (action != null) {
            HBox row = new HBox(action);
            row.setAlignment(Pos.CENTER);
            getChildren().add(row);
        }
    }

    public EmptyState(String title, String hint) {
        this(title, hint, null);
    }
}

package view.components;

import javafx.scene.control.Label;

/**
 * PreviewBadge — a small, honest "Preview / sample data" chip.
 *
 * <p>Per the data-honesty rule: any screen still showing illustrative (not yet
 * real) data must say so. Drop this into a {@link ScreenTitle} action slot.</p>
 */
public class PreviewBadge extends Label {

    public PreviewBadge() { this("Preview — sample data"); }

    public PreviewBadge(String text) {
        super(text);
        getStyleClass().add("preview-badge");
    }
}

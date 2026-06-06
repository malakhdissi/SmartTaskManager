package view.components;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * ProgressCard — labeled progress block (label + 0..1 ratio + optional caption).
 *
 * <p>Reused by Habits, Distractions, Goals, and any place a calm progress
 * indicator helps the user see direction without pressure.</p>
 */
public class ProgressCard extends VBox {

    public ProgressCard(String title, double ratio, String caption) {
        this(title, ratio, caption, "primary");
    }

    public ProgressCard(String title, double ratio, String caption, String accent) {
        getStyleClass().add("progress-card");
        setSpacing(8);

        HBox header = new HBox(8);
        Label t = new Label(title);
        t.getStyleClass().add("text-body");
        Region g = new Region();
        HBox.setHgrow(g, Priority.ALWAYS);
        Label pct = new Label(Math.round(Math.max(0, Math.min(1, ratio)) * 100) + "%");
        pct.getStyleClass().addAll("text-body", "text-" + accent);
        header.getChildren().addAll(t, g, pct);

        ProgressBar bar = new ProgressBar(Math.max(0, Math.min(1, ratio)));
        bar.setMaxWidth(Double.MAX_VALUE);

        getChildren().addAll(header, bar);

        if (caption != null && !caption.isBlank()) {
            Label c = new Label(caption);
            c.getStyleClass().add("text-muted");
            getChildren().add(c);
        }
    }
}

package view.components;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Kpi;

/**
 * KpiCard — a single KPI tile (label + big value + small delta).
 *
 * <p>Driven entirely from a {@link Kpi} value object. The card itself has
 * no idea where the number comes from, which is exactly the point.</p>
 */
public class KpiCard extends VBox {

    public KpiCard(Kpi kpi) {
        getStyleClass().add("kpi-card");
        setSpacing(8);

        Label label = new Label(kpi.getLabel());
        label.getStyleClass().add("kpi-label");

        Label value = new Label(kpi.getValue());
        value.getStyleClass().add("kpi-value");

        HBox bottom = new HBox(8);
        bottom.getChildren().add(value);
        if (kpi.getDelta() != null && !kpi.getDelta().isBlank()) {
            Label delta = new Label(kpi.getDelta());
            delta.getStyleClass().add("kpi-delta");
            delta.getStyleClass().add("text-" + (kpi.getAccent() == null ? "muted" : kpi.getAccent()));
            Region g = new Region();
            HBox.setHgrow(g, javafx.scene.layout.Priority.ALWAYS);
            bottom.getChildren().addAll(g, delta);
        }

        getChildren().addAll(label, bottom);
    }
}

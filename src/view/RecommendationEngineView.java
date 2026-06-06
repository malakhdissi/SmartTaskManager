package view;

import controller.NavigationController;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.Recommendation;
import service.ServiceLocator;
import view.components.ActionButton;
import view.components.ScreenTitle;

import java.util.List;

/**
 * RecommendationEngineView — explains why each suggested task is suggested.
 *
 * <p>Every row shows score, confidence, and reasoning. We expose the
 * "intelligence" of the product transparently so users trust it.</p>
 */
public class RecommendationEngineView {

    private final NavigationController nav;
    public RecommendationEngineView(NavigationController nav) { this.nav = nav; }

    public Node build() {
        VBox root = new VBox(16);
        root.getChildren().add(new ScreenTitle("Recommendation Engine",
                "Suggestions with reasoning — never a black box."));

        List<Recommendation> recs = ServiceLocator.recommendationService().getTopN(6);
        for (Recommendation r : recs) root.getChildren().add(rowFor(r));

        return root;
    }

    private Node rowFor(Recommendation r) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card-elevated");

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        Label name = new Label(r.getTask().getTitle());
        name.getStyleClass().add("task-title");
        Label score = new Label("Score " + Math.round(r.getTask().getScore()));
        score.getStyleClass().addAll("tag", "tag-intel");
        Region g = new Region(); HBox.setHgrow(g, Priority.ALWAYS);
        top.getChildren().addAll(name, g, score);

        Label why = new Label("Reason: " + r.getReason());
        why.getStyleClass().add("text-muted");
        why.setWrapText(true);

        HBox conf = new HBox(10);
        conf.setAlignment(Pos.CENTER_LEFT);
        Label cl = new Label("Confidence");
        cl.getStyleClass().add("text-muted");
        ProgressBar pb = new ProgressBar(r.getConfidence());
        pb.setPrefWidth(220);
        Label cv = new Label(Math.round(r.getConfidence() * 100) + "%");
        cv.getStyleClass().add("text-body");
        Region g2 = new Region(); HBox.setHgrow(g2, Priority.ALWAYS);
        ActionButton open = ActionButton.ghost("View task");
        open.setOnAction(e -> nav.showTaskDetails(r.getTask().getId()));
        ActionButton start = ActionButton.primary("Start focus");
        start.setOnAction(e -> nav.showDeepWork());
        conf.getChildren().addAll(cl, pb, cv, g2, open, start);

        card.getChildren().addAll(top, why, conf);
        return card;
    }
}

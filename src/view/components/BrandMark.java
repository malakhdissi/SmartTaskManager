package view.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import util.Constants;

import java.io.InputStream;

/**
 * BrandMark — the single source of truth for the STM identity (monogram +
 * wordmark). Replaces the three hand-typed text logos that used to drift.
 *
 * <p>Loads {@code /images/stm-monogram.png}; if the asset is missing it
 * degrades gracefully to the wordmark alone, so the UI never breaks.</p>
 */
public class BrandMark extends HBox {

    public enum Size { LARGE, COMPACT }

    public BrandMark(Size size) {
        getStyleClass().add("brand-mark");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(size == Size.LARGE ? 14 : 10);

        ImageView logo = loadLogo(size == Size.LARGE ? 44 : 26);
        if (logo != null) getChildren().add(logo);

        Label name = new Label(Constants.APP_NAME);
        name.getStyleClass().add(size == Size.LARGE ? "brand-mark-name-lg" : "brand-mark-name");
        getChildren().add(name);
    }

    private ImageView loadLogo(double px) {
        try (InputStream in = getClass().getResourceAsStream(Constants.LOGO_PATH)) {
            if (in == null) return null;
            ImageView iv = new ImageView(new Image(in));
            iv.setFitHeight(px);
            iv.setFitWidth(px);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            return iv;
        } catch (Exception e) {
            return null; // missing/corrupt asset → wordmark only
        }
    }
}

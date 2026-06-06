package view;

import javafx.scene.Scene;

/**
 * ThemeManager — toggles a global light/dark theme by adding/removing the
 * {@code theme-light} style class on the scene root.
 *
 * <p>Presentation-only: the palette is token-based in CSS, so flipping one
 * class re-colours the whole app. The class lives on the root node (which is
 * never rebuilt during navigation), so the chosen theme persists across screen
 * swaps. Dark is the default.</p>
 */
public final class ThemeManager {

    private static final String LIGHT_CLASS = "theme-light";
    private static boolean light = false;

    private ThemeManager() {}

    public static boolean isLight() { return light; }

    /** Flips the theme on the given scene and returns the new state (true = light). */
    public static boolean toggle(Scene scene) {
        light = !light;
        apply(scene);
        return light;
    }

    /** Re-applies the current theme to a scene (safe to call any time). */
    public static void apply(Scene scene) {
        if (scene == null || scene.getRoot() == null) return;
        var classes = scene.getRoot().getStyleClass();
        classes.remove(LIGHT_CLASS);
        if (light) classes.add(LIGHT_CLASS);
    }
}

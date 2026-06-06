package view.components;

import javafx.scene.control.Button;

/**
 * ActionButton — typed JavaFX button that maps semantically to a CSS variant.
 *
 * <p>Why: we never want a view to write {@code btn.setStyle("-fx-background-color: ...")}.
 * Variants live in style.css; views just say "make me a primary button".</p>
 */
public class ActionButton extends Button {

    public enum Variant { PRIMARY, SUCCESS, WARNING, DANGER, GHOST, NEUTRAL }

    public ActionButton(String text, Variant variant) {
        super(text);
        getStyleClass().add("btn");
        switch (variant) {
            case PRIMARY -> getStyleClass().add("btn-primary");
            case SUCCESS -> getStyleClass().add("btn-success");
            case WARNING -> getStyleClass().add("btn-warning");
            case DANGER  -> getStyleClass().add("btn-danger");
            case GHOST   -> getStyleClass().add("btn-ghost");
            case NEUTRAL -> { /* base .btn only */ }
        }
        setFocusTraversable(false);
    }

    /** Convenience factory for the most common case. */
    public static ActionButton primary(String text)  { return new ActionButton(text, Variant.PRIMARY); }
    public static ActionButton ghost(String text)    { return new ActionButton(text, Variant.GHOST); }
    public static ActionButton success(String text)  { return new ActionButton(text, Variant.SUCCESS); }
    public static ActionButton neutral(String text)  { return new ActionButton(text, Variant.NEUTRAL); }
    public static ActionButton warning(String text)  { return new ActionButton(text, Variant.WARNING); }

    /**
     * Fluent helper — runs the consumer on this button and returns this.
     * Lets views chain {@code ActionButton.primary("Save").apply(b -> b.setOnAction(...))}
     * without declaring a local variable.
     */
    public ActionButton apply(java.util.function.Consumer<ActionButton> consumer) {
        consumer.accept(this);
        return this;
    }
}

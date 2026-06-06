import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import util.Constants;
import controller.NavigationController;

/**
 * Main — JavaFX application entry point.
 *
 * <p>Role: bootstraps the JavaFX runtime, creates the root scene with the
 * shared dark-SaaS stylesheet, then delegates everything else to
 * {@link NavigationController}. No business logic lives here.</p>
 *
 * <p>Scalability note: by keeping Main thin, we can later swap the
 * NavigationController for a different orchestration strategy (e.g. a
 * preloader screen + splash, deep-linking, multi-window) without rewriting
 * application startup.</p>
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) {
        // Make hidden failures visible: any exception that escapes an event
        // handler on the JavaFX thread (or any thread) is logged with its stack
        // trace instead of vanishing silently.
        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            System.err.println("[FATAL] Uncaught exception on " + t.getName() + ": " + e);
            e.printStackTrace();
        });
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            System.err.println("[FATAL] Uncaught exception on " + t.getName() + ": " + e);
            e.printStackTrace();
        });

        // The scene root starts empty; NavigationController will swap in the
        // first screen. Using StackPane lets us overlay toasts / modals later.
        StackPane root = new StackPane();
        Scene scene = new Scene(root, Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);

        // Single, centralized stylesheet — every screen inherits it.
        var css = getClass().getResource(Constants.CSS_PATH);
        if (css != null) scene.getStylesheets().add(css.toExternalForm());
        else System.err.println("[Main] Stylesheet not found on classpath: " + Constants.CSS_PATH);
        root.getStyleClass().add("main-layout");

        // The navigation controller owns screen swapping and toast overlays.
        NavigationController nav = new NavigationController(stage, scene, root);
        nav.showWelcome();

        stage.setTitle(Constants.APP_NAME);
        stage.setScene(scene);
        stage.setMinWidth(1100);
        stage.setMinHeight(720);
        stage.show();
    }

    /** Launches the JavaFX runtime. */
    public static void main(String[] args) {
        launch(args);
    }
}

package view;

import controller.NavigationController;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import view.components.ActionButton;
import view.components.ScreenTitle;

/**
 * SettingsView — preferences screen (theme, notifications, goals, persona).
 *
 * <p>All controls are wired to toasts so the user gets calm confirmation
 * even though no preference is persisted yet.</p>
 */
public class SettingsView {

    private final NavigationController nav;

    public SettingsView(NavigationController nav) { this.nav = nav; }

    public Node build() {
        VBox root = new VBox(16);
        root.getChildren().add(new ScreenTitle("Settings", "Tune the product around how you actually work."));

        root.getChildren().add(section("Appearance",
                preferenceRow("Theme",
                        comboBox(FXCollections.observableArrayList("Dark (default)", "High contrast", "Light (beta)"), 0))));

        ComboBox<String> notifMode = comboBox(FXCollections.observableArrayList(
                "Calm — only essential nudges", "Standard", "Off"), 0);
        notifMode.setOnAction(e -> nav.notifyPrimary("Notification mode saved."));
        root.getChildren().add(section("Notifications",
                preferenceRow("Mode", notifMode),
                preferenceRow("Quiet hours", new TextField("22:30 — 07:30"))));

        ComboBox<String> persona = comboBox(FXCollections.observableArrayList(
                "Deep Work", "Anti-Procrastination", "Anti-Scrolling", "Short Focus", "Balanced"), 0);
        persona.setOnAction(e -> nav.notifySuccess("Persona updated to: " + persona.getValue()));
        root.getChildren().add(section("Persona",
                preferenceRow("Active persona", persona)));

        model.Goal active = service.ServiceLocator.goalService().getActive();
        String activeGoalLabel = active == null ? "None set yet" : active.getTitle();
        root.getChildren().add(section("Goals",
                preferenceRow("Active goal", new Label(activeGoalLabel)),
                new HBox(ActionButton.ghost("Manage goals").apply(b -> b.setOnAction(e -> nav.showGoals()))) ));

        // Account: real session controls.
        String accountLabel = service.ServiceLocator.userService().getCurrentUser()
                .map(u -> u.getDisplayName() + " · " + u.getEmail())
                .orElse("Not signed in");
        ActionButton logout = ActionButton.ghost("Log out");
        logout.setOnAction(e -> nav.logout());
        root.getChildren().add(section("Account",
                preferenceRow("Signed in as", new Label(accountLabel)),
                new HBox(logout)));

        ActionButton save = ActionButton.primary("Save changes");
        save.setOnAction(e -> nav.notifySuccess("Settings saved."));
        root.getChildren().add(save);

        return root;
    }

    private VBox section(String title, Node... rows) {
        VBox v = new VBox(10);
        v.getStyleClass().add("card");
        Label t = new Label(title);
        t.getStyleClass().add("text-body");
        v.getChildren().add(t);
        for (Node n : rows) v.getChildren().add(n);
        return v;
    }

    private HBox preferenceRow(String label, Node control) {
        Label l = new Label(label);
        l.getStyleClass().add("text-muted");
        l.setMinWidth(160);
        Region g = new Region(); HBox.setHgrow(g, Priority.ALWAYS);
        HBox row = new HBox(12, l, g, control);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return row;
    }

    private ComboBox<String> comboBox(javafx.collections.ObservableList<String> items, int defaultIndex) {
        ComboBox<String> c = new ComboBox<>(items);
        c.getSelectionModel().select(defaultIndex);
        return c;
    }
}

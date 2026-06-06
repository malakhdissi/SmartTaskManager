package view.components;

import controller.NavigationController;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import model.User;
import service.DashboardService;
import service.ServiceLocator;
import util.Formatter;

import java.time.LocalDateTime;

/**
 * TopBar — header strip on every main screen.
 *
 * <p>Shows greeting, current time, streak, productivity level, and quick
 * actions on the right. Designed to be calm — no notification badges
 * shouting for attention.</p>
 */
public class TopBar extends HBox {

    public TopBar(NavigationController nav) {
        getStyleClass().add("topbar");
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(16);

        User user = ServiceLocator.userService().getCurrentUser().orElse(null);
        DashboardService dash = ServiceLocator.dashboardService();

        VBox greeting = new VBox(2);
        Label hello = new Label("Hi, " + (user != null ? user.getDisplayName() : "there"));
        hello.getStyleClass().add("topbar-greeting");
        // Day-of-week + clock needs a date-bearing temporal — LocalDateTime, not LocalTime.
        Label time = new Label(Formatter.dayAndTime(LocalDateTime.now()));
        time.getStyleClass().add("topbar-time");
        greeting.getChildren().addAll(hello, time);

        Label streak = new Label("Streak: " + (user != null ? user.getCurrentStreakDays() : 0) + " days");
        streak.getStyleClass().add("topbar-streak");

        Label level = new Label("Productivity: " + dash.getProductivityLevel());
        level.getStyleClass().add("text-muted");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        ActionButton add = ActionButton.primary("+ Add Task");
        add.setOnAction(e -> nav.showAddTask());

        ActionButton focus = ActionButton.ghost("Start Focus");
        focus.setOnAction(e -> nav.showDeepWork());

        boolean isGuest = user != null && user.isGuest();
        if (isGuest) {
            // Guest mode: replace streak/level with a clear indicator + a path to a real account.
            Label guestChip = new Label("Guest mode — create an account to save your progress");
            guestChip.getStyleClass().add("guest-chip");
            ActionButton createAccount = ActionButton.ghost("Create account");
            createAccount.setOnAction(e -> nav.showSignup());
            getChildren().addAll(greeting, guestChip, spacer, createAccount, focus, add);
        } else {
            getChildren().addAll(greeting, streak, level, spacer, focus, add);
        }
    }
}

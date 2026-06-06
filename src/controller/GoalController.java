package controller;

import model.Goal;
import model.GoalCategory;
import model.GoalStatus;
import service.GoalService;
import service.ServiceLocator;

import java.time.LocalDate;
import java.util.UUID;

/**
 * GoalController — UI-facing write actions for goals. Views collect inputs and
 * call these; persistence goes through {@link GoalService} → DAO → DB. No
 * business logic in the view; no fake data created here.
 */
public class GoalController {

    private final NavigationController nav;
    private final GoalService goals = ServiceLocator.goalService();

    public GoalController(NavigationController nav) { this.nav = nav; }

    public void create(String title, String description, GoalCategory category, int importance, LocalDate targetDate) {
        if (title == null || title.isBlank()) { nav.notifyWarning("Goal title is required."); return; }
        Goal g = new Goal(UUID.randomUUID().toString().substring(0, 8),
                title.trim(), description == null ? "" : description.trim(),
                category, clampImportance(importance), targetDate, GoalStatus.ACTIVE, 0d, false);
        goals.save(g);
        nav.notifySuccess("Goal created.");
        nav.showGoals();
    }

    public void update(String id, String title, String description, GoalCategory category,
                       int importance, LocalDate targetDate, GoalStatus status) {
        Goal g = goals.getById(id);
        if (g == null) { nav.notifyWarning("Goal not found."); return; }
        if (title == null || title.isBlank()) { nav.notifyWarning("Goal title is required."); return; }
        g.setTitle(title.trim());
        g.setDescription(description == null ? "" : description.trim());
        g.setCategory(category);
        g.setImportance(clampImportance(importance));
        g.setTargetDate(targetDate);
        if (status != null) g.setStatus(status);
        goals.save(g);
        nav.notifySuccess("Goal updated.");
        nav.showGoals();
    }

    public void delete(String id) {
        if (goals.delete(id)) nav.notifyPrimary("Goal deleted.");
        else nav.notifyWarning("Could not delete goal.");
        nav.showGoals();
    }

    public void setActive(String id) {
        goals.setActive(id);
        nav.notifySuccess("Active goal updated.");
        nav.showGoals();
    }

    private static int clampImportance(int i) { return Math.max(1, Math.min(5, i)); }
}

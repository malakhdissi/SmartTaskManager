package controller;

import model.*;
import service.ServiceLocator;
import service.TaskService;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

/**
 * TaskUiController — the controller that views call when the user interacts
 * with task data (create, edit, delete, mark-as-done).
 *
 * <p>It holds no UI state. Instead it talks to {@link TaskService} and pings
 * the {@link NavigationController} to update toasts + screens. This is the
 * cleanest separation we can give the UI layer: views collect inputs,
 * controllers translate them into service calls, services do the work.</p>
 */
public class TaskUiController {

    private final NavigationController nav;
    private final TaskService tasks = ServiceLocator.taskService();

    public TaskUiController(NavigationController nav) {
        this.nav = nav;
    }

    /**
     * Creates a new task from form inputs. All validation lives here, not
     * in the view's event handler.
     */
    public Task createTask(String title,
                           String description,
                           Priority priority,
                           TaskStatus status,
                           TaskType type,
                           LocalDate deadline,
                           int durationMinutes,
                           TaskTemporalType temporalType,
                           String goalId,
                           int contributionPercent) {
        if (title == null || title.isBlank()) {
            nav.notifyWarning("Title is required.");
            return null;
        }
        // Real goal contribution from the form (0..1); 0 when not linked to a goal.
        double contribution = goalId == null ? 0.0 : Math.max(0, Math.min(100, contributionPercent)) / 100.0;
        Task t = new Task(
                UUID.randomUUID().toString().substring(0, 8),
                title.trim(),
                description == null ? "" : description.trim(),
                priority == null ? Priority.MEDIUM : priority,
                status == null ? TaskStatus.TODO : status,
                type == null ? TaskType.OTHER : type,
                deadline,
                durationMinutes > 0 ? Duration.ofMinutes(durationMinutes) : Duration.ofMinutes(30),
                0.0,
                contribution);
        t.setTemporalType(temporalType); // may be null → derived from TaskType
        t.setGoalId(goalId);             // null → unlinked
        Task saved = tasks.save(t);
        nav.notifySuccess("Task added: " + saved.getTitle());
        nav.showTaskList();
        return saved;
    }

    /** Updates an existing task from edit form inputs. */
    public Task updateTask(String id,
                           String title,
                           String description,
                           Priority priority,
                           TaskStatus status,
                           TaskType type,
                           LocalDate deadline,
                           int durationMinutes) {
        Task existing = tasks.getById(id).orElse(null);
        if (existing == null) {
            nav.notifyWarning("Task not found.");
            return null;
        }
        if (title == null || title.isBlank()) {
            nav.notifyWarning("Title is required.");
            return existing;
        }
        existing.setTitle(title.trim());
        existing.setDescription(description == null ? "" : description.trim());
        existing.setPriority(priority);
        existing.setStatus(status);
        existing.setType(type);
        existing.setDeadline(deadline);
        existing.setEstimatedDuration(durationMinutes > 0 ? Duration.ofMinutes(durationMinutes) : Duration.ofMinutes(30));
        Task saved = tasks.save(existing);
        nav.notifySuccess("Changes saved.");
        nav.showTaskList();
        return saved;
    }

    /** Marks a task as DONE and refreshes the current screen. */
    public void markDone(String id) {
        tasks.markDone(id).ifPresent(t -> nav.notifySuccess("Marked done: " + t.getTitle()));
        // Cheap refresh: re-show whichever list-like screen the user is on.
        // For the MVP we send them back to the Dashboard.
        nav.showDashboard();
    }

    /** Deletes a task with explicit toast feedback. */
    public void delete(String id) {
        if (tasks.delete(id)) nav.notifyPrimary("Task removed.");
        else nav.notifyWarning("Could not delete task.");
        nav.showTaskList();
    }
}

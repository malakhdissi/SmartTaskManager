package service;

import model.Task;

import java.util.List;
import java.util.Optional;

/**
 * TaskService — the only API the UI uses to read/write tasks.
 *
 * <p>Views call this; views never touch DAOs directly. That single rule is
 * what guarantees we can swap persistence later without rewriting screens.</p>
 */
public interface TaskService {
    List<Task> getAllTasks();
    Optional<Task> getById(String id);
    /** Returns the top N tasks by score, excluding DONE/SKIPPED. */
    List<Task> getTopTasks(int n);
    Task save(Task task);
    boolean delete(String id);
    /** Convenience helper used by the "Mark as done" buttons. */
    Optional<Task> markDone(String id);
}

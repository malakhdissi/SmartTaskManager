package dao;

import model.Task;

import java.util.List;
import java.util.Optional;

/**
 * TaskDao — persistence contract for tasks.
 *
 * <p>This interface deliberately uses only domain types ({@link Task}). The
 * presentation layer never imports a DAO implementation directly, only
 * services depend on this interface. That decoupling lets us move from
 * in-memory → SQLite → MySQL → cloud without touching screens.</p>
 */
public interface TaskDao {
    List<Task> findAll();
    Optional<Task> findById(String id);
    Task save(Task task);     // create or update
    boolean deleteById(String id);
}

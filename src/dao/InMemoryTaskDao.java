package dao;

import model.Task;
import service.Session;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * InMemoryTaskDao — non-persistent, per-user fallback (DB offline / tests).
 *
 * <p>No mock seeding: a new user starts with an empty list, exactly as they
 * would against the real database. Tasks are partitioned per user id so the
 * fallback still behaves like a multi-user store within a single run.</p>
 */
public class InMemoryTaskDao implements TaskDao {

    private final Session session;
    /** userId → (taskId → task), insertion-ordered for stable UI. */
    private final Map<String, Map<String, Task>> byUser = new LinkedHashMap<>();

    public InMemoryTaskDao(Session session) { this.session = session; }

    private Map<String, Task> store() {
        String uid = session.currentUserId();
        if (uid == null) return new LinkedHashMap<>(); // detached, not logged in
        return byUser.computeIfAbsent(uid, k -> new LinkedHashMap<>());
    }

    @Override public List<Task> findAll() { return new ArrayList<>(store().values()); }

    @Override public Optional<Task> findById(String id) { return Optional.ofNullable(store().get(id)); }

    @Override public Task save(Task task) { store().put(task.getId(), task); return task; }

    @Override public boolean deleteById(String id) { return store().remove(id) != null; }
}

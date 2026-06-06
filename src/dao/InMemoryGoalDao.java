package dao;

import model.Goal;
import service.Session;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * InMemoryGoalDao — non-persistent, per-user fallback (DB offline / tests).
 * No seeding: a new user starts with no goals.
 */
public class InMemoryGoalDao implements GoalDao {

    private final Session session;
    private final Map<String, Map<String, Goal>> byUser = new LinkedHashMap<>();

    public InMemoryGoalDao(Session session) { this.session = session; }

    private Map<String, Goal> store() {
        String uid = session.currentUserId();
        if (uid == null) return new LinkedHashMap<>();
        return byUser.computeIfAbsent(uid, k -> new LinkedHashMap<>());
    }

    @Override public List<Goal> findAll() { return new ArrayList<>(store().values()); }

    @Override public Optional<Goal> findById(String id) { return Optional.ofNullable(store().get(id)); }

    @Override public Optional<Goal> findActive() {
        return store().values().stream().filter(Goal::isActive).findFirst();
    }

    @Override public Goal save(Goal goal) { store().put(goal.getId(), goal); return goal; }

    @Override public boolean deleteById(String id) { return store().remove(id) != null; }

    @Override public void setActive(String id) {
        for (Goal g : store().values()) g.setActive(g.getId().equals(id));
    }
}

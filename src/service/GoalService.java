package service;

import dao.GoalDao;
import model.Goal;

import java.util.List;

/**
 * GoalService — CRUD + activation for the authenticated user's goals.
 *
 * <p>Backed by a per-user {@link GoalDao}: a new account has no goals until it
 * defines one. Progress is <em>not</em> set here — it is computed from linked
 * tasks by {@link GoalProgressService}.</p>
 */
public class GoalService {

    private final GoalDao dao;

    public GoalService(GoalDao dao) { this.dao = dao; }

    public List<Goal> getAll() { return dao.findAll(); }

    public Goal getById(String id) { return dao.findById(id).orElse(null); }

    /** The active goal, or {@code null} if the user hasn't set one yet. */
    public Goal getActive() { return dao.findActive().orElse(null); }

    /** Create or update a goal. */
    public Goal save(Goal goal) { return dao.save(goal); }

    /** Alias kept for existing callers. */
    public Goal addGoal(Goal g) { return dao.save(g); }

    public boolean delete(String id) { return dao.deleteById(id); }

    public void setActive(String id) { dao.setActive(id); }
}

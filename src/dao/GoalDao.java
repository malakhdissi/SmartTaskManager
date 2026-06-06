package dao;

import model.Goal;

import java.util.List;
import java.util.Optional;

/**
 * GoalDao — per-user persistence contract for goals.
 *
 * <p>Like {@link TaskDao}, implementations scope every operation to the
 * authenticated user, so a new account starts with no goals.</p>
 */
public interface GoalDao {

    List<Goal> findAll();

    Optional<Goal> findById(String id);

    Optional<Goal> findActive();

    /** Inserts or updates a goal (by id). */
    Goal save(Goal goal);

    boolean deleteById(String id);

    /** Marks the given goal id active and all the user's other goals inactive. */
    void setActive(String id);
}

package dao;

import model.User;

import java.util.Optional;

/**
 * UserDao — persistence contract for user accounts.
 *
 * <p>A real multi-user store: look up by email for login, check existence for
 * signup uniqueness, and persist (insert or update). The presentation layer
 * never imports an implementation — only services depend on this interface, so
 * swapping in-memory ⇄ JDBC is a single wiring change in ServiceLocator.</p>
 */
public interface UserDao {

    /** Finds an account by (already-normalized) email, if one exists. */
    Optional<User> findByEmail(String email);

    /** True if an account with this (normalized) email exists. */
    boolean existsByEmail(String email);

    /** Inserts a new user or updates an existing one (by id). Returns the saved user. */
    User save(User user);
}

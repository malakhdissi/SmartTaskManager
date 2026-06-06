package service;

import model.User;

import java.util.Optional;

/**
 * Session — holds the currently authenticated user for the app's lifetime.
 *
 * <p>Before this existed, "current user" was baked into the DAO (a mock
 * singleton). Now identity is a runtime concept owned by the service layer:
 * {@link AuthService} sets it on login/signup, DAOs read it to scope data to
 * the logged-in user, and {@link UserService} exposes it to the UI.</p>
 */
public final class Session {

    private User currentUser;

    public Optional<User> currentUser() { return Optional.ofNullable(currentUser); }

    /** The logged-in user's id, or {@code null} if nobody is authenticated. */
    public String currentUserId() { return currentUser == null ? null : currentUser.getId(); }

    public boolean isAuthenticated() { return currentUser != null; }

    void set(User user) { this.currentUser = user; }
    void clear() { this.currentUser = null; }
}

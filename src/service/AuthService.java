package service;

import dao.UserDao;
import model.User;
import util.PasswordHasher;
import util.PasswordStrength;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * AuthService — real credential-based authentication.
 *
 * <p>Validates against the {@link UserDao}, hashes passwords with
 * {@link PasswordHasher}, and records the authenticated user in {@link Session}.
 * Returns an {@link AuthResult} so the UI can show precise outcomes
 * (not-found, wrong-password, email-exists) rather than a single generic error.</p>
 */
public class AuthService {

    /** Pragmatic email shape check — not RFC-perfect, but rejects obvious garbage. */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final int NAME_MIN = 2;
    private static final int NAME_MAX = 80;
    private static final int EMAIL_MAX = 255;

    private final UserDao userDao;
    private final PasswordHasher hasher;
    private final Session session;

    public AuthService(UserDao userDao, PasswordHasher hasher, Session session) {
        this.userDao = userDao;
        this.hasher = hasher;
        this.session = session;
    }

    /** Authenticates an existing account. */
    public AuthResult login(String email, char[] password) {
        if (isBlank(email) || password == null || password.length == 0) {
            return AuthResult.failure(AuthResult.Status.INVALID_INPUT, "Please enter your email and password.");
        }
        String normalized = normalize(email);
        try {
            Optional<User> found = userDao.findByEmail(normalized);
            if (found.isEmpty()) {
                return AuthResult.failure(AuthResult.Status.USER_NOT_FOUND, "No account found for that email.");
            }
            User user = found.get();
            if (!hasher.verify(password, user.getPasswordHash())) {
                return AuthResult.failure(AuthResult.Status.WRONG_PASSWORD, "Incorrect password. Please try again.");
            }
            session.set(user);
            System.out.println("[AuthService] login OK, session set for " + user.getEmail());
            return AuthResult.success(user);
        } catch (Exception e) {
            System.err.println("[AuthService] login failed: " + e);
            e.printStackTrace();
            return AuthResult.failure(AuthResult.Status.ERROR, "Something went wrong. Please try again.");
        }
    }

    /** Creates a new account, then signs it in. */
    public AuthResult signup(String displayName, String email, char[] password) {
        if (isBlank(displayName) || displayName.trim().length() < NAME_MIN) {
            return AuthResult.failure(AuthResult.Status.INVALID_INPUT, "Please enter your name (at least " + NAME_MIN + " characters).");
        }
        if (displayName.trim().length() > NAME_MAX) {
            return AuthResult.failure(AuthResult.Status.INVALID_INPUT, "That name is too long.");
        }
        if (isBlank(email) || email.trim().length() > EMAIL_MAX || !EMAIL.matcher(email.trim()).matches()) {
            return AuthResult.failure(AuthResult.Status.INVALID_INPUT, "Please enter a valid email address.");
        }
        String pwError = PasswordStrength.policyError(password);
        if (pwError != null) {
            return AuthResult.failure(AuthResult.Status.INVALID_INPUT, pwError);
        }
        String normalized = normalize(email);
        try {
            if (userDao.existsByEmail(normalized)) {
                return AuthResult.failure(AuthResult.Status.EMAIL_ALREADY_EXISTS,
                        "An account with that email already exists.");
            }
            User user = new User(
                    UUID.randomUUID().toString(),
                    displayName.trim(),
                    normalized,
                    hasher.hash(password),
                    0,
                    null);
            userDao.save(user);
            session.set(user);
            System.out.println("[AuthService] signup OK, session set for " + user.getEmail());
            return AuthResult.success(user);
        } catch (Exception e) {
            System.err.println("[AuthService] signup failed: " + e);
            e.printStackTrace();
            return AuthResult.failure(AuthResult.Status.ERROR, "Could not create your account. Please try again.");
        }
    }

    /**
     * Starts an anonymous guest session. The guest is a real, flagged, empty
     * account (so it works with the existing per-user DAO/FK design) — not mock
     * data. It has no usable password.
     */
    public AuthResult continueAsGuest() {
        try {
            String id = UUID.randomUUID().toString();
            String email = "guest-" + id.substring(0, 8) + "@guest.local";
            User guest = new User(id, "Guest", email, hasher.hash(("guest-" + id).toCharArray()), 0, null);
            guest.setGuest(true);
            userDao.save(guest);
            session.set(guest);
            System.out.println("[AuthService] guest session started: " + email);
            return AuthResult.success(guest);
        } catch (Exception e) {
            System.err.println("[AuthService] guest start failed: " + e);
            e.printStackTrace();
            return AuthResult.failure(AuthResult.Status.ERROR, "Couldn't start guest mode. Please try again.");
        }
    }

    public void logout() { session.clear(); }

    public Optional<User> currentUser() { return session.currentUser(); }

    public boolean isAuthenticated() { return session.isAuthenticated(); }

    private static boolean isBlank(String s) { return s == null || s.isBlank(); }

    private static String normalize(String email) { return email.trim().toLowerCase(); }
}

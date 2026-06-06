package service;

import dao.InMemoryUserDao;
import dao.UserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.PasswordHasher;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for the auth → session flow that the post-login navigation
 * depends on. No JavaFX or database involved — pure service logic against the
 * in-memory DAO.
 */
class AuthFlowTest {

    private Session session;
    private AuthService auth;

    @BeforeEach
    void setUp() {
        session = new Session();
        UserDao userDao = new InMemoryUserDao();
        auth = new AuthService(userDao, new PasswordHasher(), session);
    }

    @Test
    void signupSucceedsAndSetsSession() {
        AuthResult r = auth.signup("Sam", "sam@example.com", "password123".toCharArray());

        assertTrue(r.isSuccess(), "signup should succeed");
        assertTrue(session.isAuthenticated(), "session must be authenticated after signup");
        assertEquals("sam@example.com", session.currentUser().orElseThrow().getEmail());
    }

    @Test
    void loginAfterSignupSucceedsAndSetsSession() {
        auth.signup("Sam", "sam@example.com", "password123".toCharArray());
        auth.logout();
        assertFalse(session.isAuthenticated(), "logout should clear the session");

        AuthResult r = auth.login("sam@example.com", "password123".toCharArray());

        assertTrue(r.isSuccess(), "login with correct credentials should succeed");
        assertTrue(session.isAuthenticated(), "login success must set the session");
        assertEquals("sam@example.com", session.currentUserId() == null
                ? null : session.currentUser().orElseThrow().getEmail());
    }

    @Test
    void loginEmailIsCaseAndSpaceInsensitive() {
        auth.signup("Sam", "Sam@Example.com ", "password123".toCharArray());
        AuthResult r = auth.login("  sam@example.com", "password123".toCharArray());
        assertTrue(r.isSuccess(), "email should be normalized for login");
    }

    @Test
    void wrongPasswordDoesNotAuthenticate() {
        auth.signup("Sam", "sam@example.com", "password123".toCharArray());
        auth.logout();

        AuthResult r = auth.login("sam@example.com", "WRONGpassword".toCharArray());

        assertEquals(AuthResult.Status.WRONG_PASSWORD, r.status());
        assertFalse(session.isAuthenticated(), "wrong password must not authenticate");
    }

    @Test
    void unknownEmailReportsUserNotFound() {
        AuthResult r = auth.login("nobody@example.com", "password123".toCharArray());
        assertEquals(AuthResult.Status.USER_NOT_FOUND, r.status());
        assertFalse(session.isAuthenticated());
    }

    @Test
    void duplicateSignupIsRejected() {
        auth.signup("Sam", "sam@example.com", "password123".toCharArray());
        AuthResult r = auth.signup("Sam Two", "sam@example.com", "password123".toCharArray());
        assertEquals(AuthResult.Status.EMAIL_ALREADY_EXISTS, r.status());
    }

    @Test
    void shortPasswordIsRejected() {
        AuthResult r = auth.signup("Sam", "sam@example.com", "short".toCharArray());
        assertEquals(AuthResult.Status.INVALID_INPUT, r.status());
        assertFalse(session.isAuthenticated());
    }
}

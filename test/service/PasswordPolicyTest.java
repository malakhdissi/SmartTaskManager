package service;

import dao.InMemoryUserDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.PasswordHasher;
import util.PasswordStrength;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates the password policy + strength rules, both at the util level and
 * as enforced by signup.
 */
class PasswordPolicyTest {

    private Session session;
    private AuthService auth;

    @BeforeEach
    void setUp() {
        session = new Session();
        auth = new AuthService(new InMemoryUserDao(), new PasswordHasher(), session);
    }

    /* ---- policy at the util level ---- */

    @Test
    void passwordWithoutDigitFailsPolicy() {
        assertFalse(PasswordStrength.meetsPolicy("abcdefgh".toCharArray()));
        assertNotNull(PasswordStrength.policyError("abcdefgh".toCharArray()));
    }

    @Test
    void passwordWithoutLetterFailsPolicy() {
        assertFalse(PasswordStrength.meetsPolicy("12345678".toCharArray()));
    }

    @Test
    void shortPasswordFailsPolicy() {
        assertFalse(PasswordStrength.meetsPolicy("ab12".toCharArray()));
    }

    @Test
    void validPasswordPassesPolicy() {
        assertTrue(PasswordStrength.meetsPolicy("password1".toCharArray()));
        assertNull(PasswordStrength.policyError("password1".toCharArray()));
    }

    @Test
    void strengthLevelsAreSensible() {
        assertEquals(PasswordStrength.Level.WEAK, PasswordStrength.level("abc".toCharArray()));
        assertEquals(PasswordStrength.Level.FAIR, PasswordStrength.level("password1".toCharArray()));
        assertEquals(PasswordStrength.Level.STRONG, PasswordStrength.level("password1!longer".toCharArray()));
    }

    /* ---- enforced through signup ---- */

    @Test
    void signupRejectsWeakPassword() {
        AuthResult r = auth.signup("Sam", "sam@example.com", "abcdefgh".toCharArray());
        assertEquals(AuthResult.Status.INVALID_INPUT, r.status());
        assertFalse(session.isAuthenticated());
    }

    @Test
    void signupRejectsShortName() {
        AuthResult r = auth.signup("S", "sam@example.com", "password1".toCharArray());
        assertEquals(AuthResult.Status.INVALID_INPUT, r.status());
    }

    @Test
    void signupRejectsInvalidEmail() {
        AuthResult r = auth.signup("Sam", "not-an-email", "password1".toCharArray());
        assertEquals(AuthResult.Status.INVALID_INPUT, r.status());
    }

    @Test
    void signupAcceptsValidInput() {
        AuthResult r = auth.signup("Sam", "sam@example.com", "password1".toCharArray());
        assertTrue(r.isSuccess());
        assertTrue(session.isAuthenticated());
    }
}

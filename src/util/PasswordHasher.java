package util;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * PasswordHasher — BCrypt hashing/verification.
 *
 * <p>Raw passwords are handled as {@code char[]} and never persisted or logged.
 * Only the salted BCrypt hash is stored. Cost factor 12 is a sensible 2026
 * default (tune up as hardware improves).</p>
 */
public class PasswordHasher {

    private static final int COST = 12;

    /** Hashes a raw password into a self-describing BCrypt string. */
    public String hash(char[] rawPassword) {
        return BCrypt.withDefaults().hashToString(COST, rawPassword);
    }

    /** Verifies a raw password against a stored BCrypt hash. */
    public boolean verify(char[] rawPassword, String storedHash) {
        if (storedHash == null || storedHash.isBlank()) return false;
        return BCrypt.verifyer().verify(rawPassword, storedHash).verified;
    }
}

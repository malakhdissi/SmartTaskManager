package util;

/**
 * PasswordStrength — single source of truth for the password policy and an
 * advisory strength rating. No UI, no dependencies — easy to unit-test and to
 * reuse from both the service (enforcement) and the view (live meter).
 *
 * <p>Policy (enforced at signup): at least {@link #MIN_LENGTH} characters,
 * containing at least one letter AND one digit, up to {@link #MAX_LENGTH}.</p>
 */
public final class PasswordStrength {

    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 100;

    public enum Level { WEAK, FAIR, STRONG }

    private PasswordStrength() {}

    /** True if the password satisfies the minimum policy required to register. */
    public static boolean meetsPolicy(char[] pw) {
        if (pw == null || pw.length < MIN_LENGTH || pw.length > MAX_LENGTH) return false;
        return hasLetter(pw) && hasDigit(pw);
    }

    /**
     * Human-readable reason the password fails policy, or {@code null} if it
     * passes. Used for inline validation messages.
     */
    public static String policyError(char[] pw) {
        if (pw == null || pw.length == 0) return "Password is required.";
        if (pw.length < MIN_LENGTH) return "Password must be at least " + MIN_LENGTH + " characters.";
        if (pw.length > MAX_LENGTH) return "Password is too long.";
        if (!hasLetter(pw)) return "Password must include at least one letter.";
        if (!hasDigit(pw)) return "Password must include at least one number.";
        return null;
    }

    /** Advisory strength used by the live meter. */
    public static Level level(char[] pw) {
        if (!meetsPolicy(pw)) return Level.WEAK;
        if (pw.length >= 12 && hasSymbol(pw)) return Level.STRONG;
        return Level.FAIR;
    }

    private static boolean hasLetter(char[] pw) {
        for (char c : pw) if (Character.isLetter(c)) return true;
        return false;
    }

    private static boolean hasDigit(char[] pw) {
        for (char c : pw) if (Character.isDigit(c)) return true;
        return false;
    }

    private static boolean hasSymbol(char[] pw) {
        for (char c : pw) if (!Character.isLetterOrDigit(c) && !Character.isWhitespace(c)) return true;
        return false;
    }
}

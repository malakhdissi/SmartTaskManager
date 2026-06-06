package dao;

/**
 * DataAccessException — unchecked wrapper for SQL failures.
 *
 * <p>Keeps checked {@link java.sql.SQLException} out of the service/UI layers
 * while still surfacing the cause. Callers that care (e.g. AuthService) can
 * catch this and translate it into a user-facing message.</p>
 */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}

package service;

import model.User;

/**
 * AuthResult — the outcome of a login/signup attempt.
 *
 * <p>Carries a {@link Status} so the UI can show a precise, honest message
 * instead of a single generic error. The richer per-field messaging and
 * password-strength UI build on this in P1 without changing the service.</p>
 */
public final class AuthResult {

    public enum Status {
        SUCCESS,
        INVALID_INPUT,        // missing/blank fields, malformed email
        USER_NOT_FOUND,       // login: no account for that email
        WRONG_PASSWORD,       // login: password mismatch
        EMAIL_ALREADY_EXISTS, // signup: email already registered
        ERROR                 // unexpected (e.g. persistence failure)
    }

    private final Status status;
    private final User user;     // non-null only when status == SUCCESS
    private final String message;

    private AuthResult(Status status, User user, String message) {
        this.status = status;
        this.user = user;
        this.message = message;
    }

    public static AuthResult success(User user) {
        return new AuthResult(Status.SUCCESS, user, "Success.");
    }

    public static AuthResult failure(Status status, String message) {
        return new AuthResult(status, null, message);
    }

    public Status status()    { return status; }
    public User user()        { return user; }
    public String message()   { return message; }
    public boolean isSuccess() { return status == Status.SUCCESS; }
}

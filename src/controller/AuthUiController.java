package controller;

import model.User;
import service.AuthResult;
import service.ServiceLocator;

import java.util.function.Consumer;

/**
 * AuthUiController — the single entry point for the login/signup/guest →
 * session → navigation flow.
 *
 * <p>Views collect inputs and delegate here. On success this verifies the
 * session and navigates to the Dashboard. On a validation/credential failure it
 * hands the {@link AuthResult} back to the view via {@code onError} so the view
 * can render an inline, field-level message. Unexpected failures (including
 * {@link Error}s) become a visible toast instead of a frozen screen.</p>
 */
public class AuthUiController {

    private final NavigationController nav;

    public AuthUiController(NavigationController nav) {
        this.nav = nav;
    }

    public void login(String email, char[] password, Consumer<AuthResult> onError) {
        try {
            handle(ServiceLocator.authService().login(email, password), "login", onError);
        } catch (Throwable t) {
            fail("login", t);
        }
    }

    public void signup(String name, String email, char[] password, Consumer<AuthResult> onError) {
        try {
            handle(ServiceLocator.authService().signup(name, email, password), "signup", onError);
        } catch (Throwable t) {
            fail("signup", t);
        }
    }

    public void guest() {
        try {
            handle(ServiceLocator.authService().continueAsGuest(), "guest", null);
        } catch (Throwable t) {
            fail("guest", t);
        }
    }

    /* ------------------------------------------------------------------ */

    private void handle(AuthResult result, String action, Consumer<AuthResult> onError) {
        if (!result.isSuccess()) {
            System.out.println("[Auth] " + action + " rejected → " + result.status() + " (" + result.message() + ")");
            if (onError != null) onError.accept(result);
            else nav.notifyWarning(result.message());
            return;
        }

        User user = result.user();
        System.out.println("[Auth] " + action + " success → " + user.getEmail());

        boolean sessionOk = ServiceLocator.authService().isAuthenticated();
        System.out.println("[Auth] session user set = " + sessionOk
                + " (" + (sessionOk ? user.getDisplayName() : "none") + ")");
        if (!sessionOk) {
            nav.notifyDanger("Signed in, but the session didn't persist. Please try again.");
            return;
        }

        nav.notifySuccess(greeting(action, user));
        System.out.println("[Auth] requesting Dashboard navigation…");
        nav.showDashboard();
    }

    private static String greeting(String action, User user) {
        return switch (action) {
            case "login"  -> "Welcome back, " + user.getDisplayName() + ".";
            case "guest"  -> "You're exploring in guest mode.";
            default        -> "Welcome, " + user.getDisplayName() + "! Let's get started.";
        };
    }

    private void fail(String action, Throwable t) {
        System.err.println("[Auth] " + action + " threw: " + t);
        t.printStackTrace();
        nav.notifyDanger("Sign-in failed unexpectedly. See logs for details.");
    }
}

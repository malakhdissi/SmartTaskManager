# P0 — Trust & Authenticity + Clean-Slate Persistence

**Status:** Implemented (pending build/run verification on a machine with Maven + MySQL).
**Scope owner objective:** Make the first impression feel like a premium SaaS product, not a student project — by replacing demo authentication with real, secure, per-user persistence.

---

## What changed and why

### 1. Real authentication (was: demo facade)
- **Before:** `AuthService.login()` accepted *any* non-blank input and returned a single hardcoded mock user. `signup()` just called `login()`. The login screen pre-filled `yassine@smarttask.app` / `demo` and displayed *"Demo mode — any credentials work."*
- **After:** Credentials are validated against a real user store. Passwords are hashed with **BCrypt (cost 12)** and never stored or logged in plaintext. Login/signup return an [`AuthResult`](../src/service/AuthResult.java) with precise statuses (`USER_NOT_FOUND`, `WRONG_PASSWORD`, `EMAIL_ALREADY_EXISTS`, `INVALID_INPUT`).
- **Identity moved to a session:** "current user" is no longer baked into the DAO. [`Session`](../src/service/Session.java) holds the authenticated user; `AuthService` sets it, DAOs read it to scope data, `UserService`/`TopBar` read it for the real greeting.

### 2. MySQL/JDBC persistence (was: in-memory mock)
- [`Database`](../src/dao/Database.java) loads [`/db.properties`](../resources/db.properties), verifies connectivity, and applies [`/db/schema.sql`](../resources/db/schema.sql) automatically on first run (tables: `users`, `tasks`, `goals`).
- JDBC DAOs ([`JdbcUserDao`](../src/dao/JdbcUserDao.java), [`JdbcTaskDao`](../src/dao/JdbcTaskDao.java), [`JdbcGoalDao`](../src/dao/JdbcGoalDao.java)) use parameterized statements (no SQL injection).
- **Graceful fallback:** if MySQL is unreachable, `ServiceLocator` wires in-memory DAOs so the UI still launches. The Welcome screen shows an honest *"Offline mode — won't be saved"* note. (Configured in [`ServiceLocator`](../src/service/ServiceLocator.java).)

### 3. Clean slate for new users (was: everyone saw "Yassine's" mock data)
- Tasks and goals are now **per-user**, scoped by session id. A brand-new account starts **empty**.
- The Dashboard is honest by construction: [`DashboardService`](../src/service/DashboardService.java) derives KPIs (Open Tasks, Completed, High Priority, Goal Progress) from the user's *real* tasks/goals — a new user sees real zeros, never a fabricated "Focus Score 74%".
- The "Best Next Action" hero shows an onboarding prompt instead of recommending a fake task.

### 4. Branding consistency
- Official tagline wired in: **"Know What To Do Next."** (was `"Decide less. Focus more. Grow daily."`). Version string no longer says "(MVP)". ([`Constants`](../src/util/Constants.java))
- New [`BrandMark`](../src/view/components/BrandMark.java) component renders the previously-unused `stm-monogram.png` + wordmark, used on Welcome/Login/Signup and the Sidebar — one source of truth instead of three hand-typed text logos.

### 5. Entry-screen redesign (was: stretched mobile form in a void)
- [`WelcomeView`](../src/view/WelcomeView.java): full-bleed **split panel** — brand + one value proposition + 3 benefit bullets on the left; two distinct CTAs (Get Started → signup, Log in → login) on the right. No placeholder illustration, no "MVP" badge, no dead space.
- [`LoginView`](../src/view/LoginView.java) rewritten (real auth, no demo wording/credentials). New [`SignupView`](../src/view/SignupView.java) (name/email/password, real validation, distinct screen). New users land on goal setup as their first onboarding step.
- Logout + real "signed in as" added to [`SettingsView`](../src/view/SettingsView.java).

---

## Scope boundary (intentionally deferred)
- **Granular inline field errors, password-strength meter, show/hide password** → P1 (the `AuthResult` seam is ready; these are UI on top).
- **Guest mode, forgot password, remember me, session persistence across restarts** → P1/P2.
- **Secondary "intelligence" screens** (Insights, Habits, Distractions, Leaderboard, Timeline, AI Coach, Smart Schedule, Temporal) still use `MockDataGenerator` and are clearly future-vision surface. They are **not** claimed as real; de-mocking them is a separate effort.

---

## How to build, run, and verify

> This machine has Java 25 but **no Maven and no MySQL**, so the steps below must be run where those exist.

**1. Add dependencies** — already in [`pom.xml`](../pom.xml): `mysql-connector-j` 8.3.0, `at.favre.lib:bcrypt` 0.10.2.

**2. Create the database** (one time):
```sql
CREATE DATABASE smarttask CHARACTER SET utf8mb4;
CREATE USER 'smarttask'@'localhost' IDENTIFIED BY 'your-password';
GRANT ALL PRIVILEGES ON smarttask.* TO 'smarttask'@'localhost';
```
Set `db.password` in `resources/db.properties`. (Tables are created automatically on first run.)

**3. Run:**
```
mvn clean javafx:run
```

**4. Manual verification checklist:**
- [ ] Welcome screen shows brand mark, "Know What To Do Next.", split layout — no "MVP"/"Demo" text, no pre-filled credentials.
- [ ] Sign up with a new email → lands on goal setup; greeting shows *your* name.
- [ ] Log out (Settings) → log back in with same credentials → success.
- [ ] Wrong password → "Incorrect password." Unknown email → "No account found." Duplicate signup → "already exists." Password < 8 chars → rejected.
- [ ] Brand-new account dashboard shows Open Tasks = 0, empty task list, onboarding hero (no fake KPIs).
- [ ] Create a task → it persists; restart the app → task is still there.
- [ ] Stop MySQL → app still launches in offline mode with the warning note.

**Backend verification already done:** the model/DAO/service layer compiles cleanly with `javac` (JDBC DAOs, Session, DashboardService, etc.). Only the JavaFX views and BCrypt files require the full dependency set to compile.

---

## Bugfix — post-login navigation could fail silently

**Symptom:** after a successful login/signup the screen stayed frozen on the auth form; nothing opened.

**Root cause:** `NavigationController.showDashboard()` built the screen *before* swapping it in: `swap(new MainLayout(new DashboardView(this).build(), …))`. If any `build()` in that chain threw, the exception propagated into the JavaFX event dispatcher, the `swap()` never executed, and the previous screen remained — a silent, invisible failure. There was also no global uncaught-exception handler, so the stack trace was easy to miss.

**Fix:**
- `NavigationController` now routes every screen through a `render(name, Supplier<Node>)` helper that builds + swaps inside a `try/catch (Throwable)`. On failure it logs the root cause, shows a danger toast, and swaps in a readable error screen instead of freezing.
- New [`AuthUiController`](../src/controller/AuthUiController.java) centralizes login/signup → session-verify → navigate-to-Dashboard, catching `Throwable` so even `Error`s (e.g. a missing dependency) surface as a toast. **Both login and signup now land on the Dashboard.**
- [`Main`](../src/Main.java) installs thread + default uncaught-exception handlers.
- [`AuthService`](../src/service/AuthService.java) now catches `Exception` (not just `RuntimeException`) and logs the cause.
- Console logs added: `login/signup success`, `session user set = …`, `navigation requested → Dashboard`, `rendered ✓ Dashboard`.

**Regression tests** (in `test/`, run with `mvn test`): [`AuthFlowTest`](../test/service/AuthFlowTest.java) (signup/login set the session; wrong password / unknown email / duplicate / short password rejected; email normalized) and [`DashboardRenderTest`](../test/service/DashboardRenderTest.java) (new-user dashboard renders honest zeros; KPIs reflect real counts).

---

## P1 — Security, Validation & Conversion

**New files:** `src/util/PasswordStrength.java`, `src/view/components/PasswordBox.java`, `src/view/ForgotPasswordView.java`, `test/service/PasswordPolicyTest.java`, `test/service/GuestModeTest.java`.
**Modified:** `User` (+`guest` flag), `JdbcUserDao` (persist/read `is_guest`), `AuthService` (password policy + `continueAsGuest()`), `AuthUiController` (`guest()` + inline-error callback), `NavigationController` (`showForgotPassword()`), `LoginView`, `SignupView`, `WelcomeView`, `TopBar`, `style.css`.

- **Validation:** name 2–80; email pattern + ≤255, normalized; password ≥8 with ≥1 letter & ≥1 digit (≤100). Enforced in `AuthService`, surfaced **inline** under the form.
- **Messages:** "No account found for that email." / "Incorrect password. Please try again." / "An account with that email already exists." / "Please enter your email and password."
- **Show/hide password:** `PasswordBox` (masked + plain fields, bound; Show/Hide toggle).
- **Strength meter:** live Weak/Fair/Strong on signup.
- **Guest mode:** "Continue as guest" → real flagged empty account (`is_guest=true`, works with per-user DAOs) → Dashboard with a "Guest mode — create an account" indicator + CTA.
- **Forgot password:** honest placeholder screen (no fake reset), link from login.

**Verified:** `javac` backend + all tests compile (exit 0). Not runnable here (no Maven/MySQL) — run `mvn clean test` and `mvn clean javafx:run` locally.

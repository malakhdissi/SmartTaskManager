package service;

import dao.InMemoryGoalDao;
import dao.InMemoryTaskDao;
import dao.InMemoryUserDao;
import model.Kpi;
import org.junit.jupiter.api.Test;
import strategy.DefaultScoringStrategy;
import util.PasswordHasher;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guest mode: an anonymous, flagged, empty session that lands on a working,
 * zero-state Dashboard — not mock data, not a crash.
 */
class GuestModeTest {

    @Test
    void continueAsGuestStartsAuthenticatedGuestSession() {
        Session session = new Session();
        AuthService auth = new AuthService(new InMemoryUserDao(), new PasswordHasher(), session);

        AuthResult r = auth.continueAsGuest();

        assertTrue(r.isSuccess(), "guest mode should start successfully");
        assertTrue(session.isAuthenticated(), "guest must have an authenticated session");
        assertTrue(r.user().isGuest(), "the guest user must be flagged is_guest");
    }

    @Test
    void guestDashboardIsEmptyZeroState() {
        Session session = new Session();
        AuthService auth = new AuthService(new InMemoryUserDao(), new PasswordHasher(), session);
        auth.continueAsGuest();

        TaskService tasks = new TaskServiceImpl(new InMemoryTaskDao(session), new DefaultScoringStrategy());
        DashboardService dash = new DashboardService(tasks, new GoalService(new InMemoryGoalDao(session)));

        List<Kpi> kpis = assertDoesNotThrow(dash::getKpis);
        assertEquals("0%", kpis.get(0).getValue());  // Focus Score
        assertEquals("0",  kpis.get(2).getValue());  // Tasks Completed
        assertEquals("Getting started", dash.getProductivityLevel());
    }
}

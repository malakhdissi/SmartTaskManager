package dao;

import model.TemporalProfile;

import java.util.Optional;

/**
 * TemporalProfileDAO — one energy-rhythm profile per user.
 * Scoped to the authenticated user via {@link service.Session}.
 */
public interface TemporalProfileDAO {
    Optional<TemporalProfile> findForCurrentUser();
    TemporalProfile save(TemporalProfile profile);
}

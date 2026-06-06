package dao;

import model.TemporalProfile;
import service.Session;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/** Non-persistent, per-user {@link TemporalProfileDAO} (DB offline / tests). */
public class InMemoryTemporalProfileDAO implements TemporalProfileDAO {

    private final Session session;
    private final Map<String, TemporalProfile> byUser = new HashMap<>();

    public InMemoryTemporalProfileDAO(Session session) { this.session = session; }

    @Override
    public Optional<TemporalProfile> findForCurrentUser() {
        String uid = session.currentUserId();
        if (uid == null) return Optional.empty();
        return Optional.ofNullable(byUser.get(uid));
    }

    @Override
    public TemporalProfile save(TemporalProfile profile) {
        String uid = session.currentUserId();
        if (uid != null) byUser.put(uid, profile);
        return profile;
    }
}

package dao;

import model.TimeBlock;
import service.Session;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Non-persistent, per-user {@link TimeBlockDAO} (DB offline / tests). */
public class InMemoryTimeBlockDAO implements TimeBlockDAO {

    private final Session session;
    private final Map<String, Map<String, TimeBlock>> byUser = new LinkedHashMap<>();

    public InMemoryTimeBlockDAO(Session session) { this.session = session; }

    private Map<String, TimeBlock> store() {
        String uid = session.currentUserId();
        if (uid == null) return new LinkedHashMap<>();
        return byUser.computeIfAbsent(uid, k -> new LinkedHashMap<>());
    }

    @Override
    public List<TimeBlock> findForCurrentUser() {
        List<TimeBlock> out = new ArrayList<>(store().values());
        out.sort(Comparator.comparing(TimeBlock::getStartTime));
        return out;
    }

    @Override public TimeBlock save(TimeBlock block) { store().put(block.getId(), block); return block; }

    @Override public boolean deleteById(String id) { return store().remove(id) != null; }
}

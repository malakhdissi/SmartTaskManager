package service;

import dao.TemporalProfileDAO;
import dao.TimeBlockDAO;
import model.EnergyLevel;
import model.TemporalProfile;
import model.TimeBlock;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * TemporalPlanningService — owns the user's temporal profile and time blocks,
 * and derives deep-work windows.
 *
 * <p>If the user hasn't customized anything yet, a sensible profile-derived set
 * of blocks is generated so the engine works immediately. These are clearly
 * "suggested" defaults computed from the user's own profile — not fabricated
 * analytics.</p>
 */
public class TemporalPlanningService {

    private final TimeBlockDAO blockDao;
    private final TemporalProfileDAO profileDao;
    private final Session session;

    public TemporalPlanningService(TimeBlockDAO blockDao, TemporalProfileDAO profileDao, Session session) {
        this.blockDao = blockDao;
        this.profileDao = profileDao;
        this.session = session;
    }

    /** The user's profile, or a sensible default if none saved yet. */
    public TemporalProfile getProfile() {
        return profileDao.findForCurrentUser()
                .orElseGet(() -> TemporalProfile.defaultFor(session.currentUserId()));
    }

    public TemporalProfile saveProfile(TemporalProfile profile) {
        return profileDao.save(profile);
    }

    /** Stored blocks, or profile-derived suggested blocks when none exist. */
    public List<TimeBlock> getBlocks() {
        List<TimeBlock> stored = blockDao.findForCurrentUser();
        return stored.isEmpty() ? suggestedBlocks() : stored;
    }

    public TimeBlock addBlock(TimeBlock block) { return blockDao.save(block); }

    public boolean deleteBlock(String id) { return blockDao.deleteById(id); }

    /** Blocks long and high-energy enough to host deep work. */
    public List<TimeBlock> detectDeepWorkWindows() {
        List<TimeBlock> out = new ArrayList<>();
        for (TimeBlock b : getBlocks()) {
            if (b.getEnergyLevel() == EnergyLevel.HIGH && b.getAvailableMinutes() >= 60) out.add(b);
        }
        return out;
    }

    /** A morning/afternoon/evening template derived from the profile's energy bands. */
    public List<TimeBlock> suggestedBlocks() {
        TemporalProfile p = getProfile();
        String uid = session.currentUserId();
        List<TimeBlock> blocks = new ArrayList<>();
        blocks.add(new TimeBlock(id(), uid, LocalTime.of(8, 0), LocalTime.of(12, 0),
                p.getPreferredMorningEnergy(), 240, "Morning (suggested)"));
        blocks.add(new TimeBlock(id(), uid, LocalTime.of(13, 0), LocalTime.of(18, 0),
                p.getPreferredAfternoonEnergy(), 300, "Afternoon (suggested)"));
        blocks.add(new TimeBlock(id(), uid, LocalTime.of(19, 0), LocalTime.of(22, 0),
                p.getPreferredEveningEnergy(), 180, "Evening (suggested)"));
        return blocks;
    }

    private static String id() { return UUID.randomUUID().toString().substring(0, 8); }
}

package service;

import dao.InMemoryTemporalProfileDAO;
import dao.InMemoryTimeBlockDAO;
import dao.TemporalProfileDAO;
import dao.TimeBlockDAO;
import model.EnergyLevel;
import model.TimeBlock;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Temporal planning: sensible defaults before customization, suggested blocks,
 * deep-work window detection, and stored blocks overriding suggestions.
 */
class TemporalPlanningTest {

    private Session session;
    private TemporalPlanningService planning;

    @BeforeEach
    void setUp() {
        session = new Session();
        session.set(new User("u1", "Sam", "sam@example.com", null, 0, null));
        TimeBlockDAO blockDao = new InMemoryTimeBlockDAO(session);
        TemporalProfileDAO profileDao = new InMemoryTemporalProfileDAO(session);
        planning = new TemporalPlanningService(blockDao, profileDao, session);
    }

    @Test
    void providesDefaultProfileBeforeCustomization() {
        assertNotNull(planning.getProfile());
        assertEquals("u1", planning.getProfile().getUserId());
    }

    @Test
    void suggestsBlocksWhenNoneStored() {
        List<TimeBlock> blocks = planning.getBlocks();
        assertEquals(3, blocks.size(), "morning/afternoon/evening suggested blocks");
    }

    @Test
    void detectsDeepWorkWindowFromDefaultProfile() {
        // Default profile: morning energy HIGH, 240-min block → qualifies.
        assertFalse(planning.detectDeepWorkWindows().isEmpty());
    }

    @Test
    void storedBlocksOverrideSuggestions() {
        planning.addBlock(new TimeBlock("b1", "u1", LocalTime.of(7, 0), LocalTime.of(8, 0),
                EnergyLevel.MEDIUM, 60, "Early start"));
        List<TimeBlock> blocks = planning.getBlocks();
        assertEquals(1, blocks.size());
        assertEquals("Early start", blocks.get(0).getLabel());
    }
}

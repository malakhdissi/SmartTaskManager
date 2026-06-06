package strategy;

import model.EnergyLevel;
import model.Priority;
import model.Recommendation;
import model.Task;
import model.TaskStatus;
import model.TaskTemporalType;
import model.TaskType;
import model.TemporalProfile;
import model.TimeBlock;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies the core temporal rules: indivisible tasks are never recommended for
 * a short gap, fixed-time-due-today wins, and deep work is preferred in a
 * high-energy window.
 */
class BasicTemporalRecommendationStrategyTest {

    private final BasicTemporalRecommendationStrategy strategy = new BasicTemporalRecommendationStrategy();
    private final LocalDateTime now = LocalDateTime.of(LocalDate.now(), LocalTime.of(9, 15));

    private Task task(String id, TaskTemporalType temporal, Priority priority, int minutes,
                      LocalDate deadline) {
        Task t = new Task(id, id, "", priority, TaskStatus.TODO, TaskType.OTHER,
                deadline, Duration.ofMinutes(minutes), 0, 0.2);
        t.setTemporalType(temporal);
        return t;
    }

    @Test
    void emptyTasksReturnsEmpty() {
        assertTrue(strategy.recommendNow(List.of(), TemporalProfile.defaultFor("u"), List.of(), now).isEmpty());
    }

    @Test
    void indivisibleTaskIsNotRecommendedForAShortGap() {
        // Current block has only 20 minutes free.
        TimeBlock shortGap = new TimeBlock("b", "u", LocalTime.of(9, 0), LocalTime.of(10, 0),
                EnergyLevel.HIGH, 20, "short");
        Task indivisible = task("indivisible", TaskTemporalType.INDIVISIBLE, Priority.HIGH, 120, null);
        Task flexible = task("flexible", TaskTemporalType.FLEXIBLE, Priority.LOW, 10, null);

        Optional<Recommendation> rec = strategy.recommendNow(
                List.of(indivisible, flexible), TemporalProfile.defaultFor("u"), List.of(shortGap), now);

        assertTrue(rec.isPresent());
        assertEquals("flexible", rec.get().getTask().getId(),
                "an indivisible 120-min task must never be picked for a 20-min gap");
    }

    @Test
    void fixedTimeTaskDueTodayIsPreferred() {
        TimeBlock block = new TimeBlock("b", "u", LocalTime.of(9, 0), LocalTime.of(12, 0),
                EnergyLevel.HIGH, 180, "morning");
        Task fixed = task("fixed", TaskTemporalType.FIXED_TIME, Priority.MEDIUM, 30, LocalDate.now());
        Task flexible = task("flexible", TaskTemporalType.FLEXIBLE, Priority.MEDIUM, 30, null);

        Optional<Recommendation> rec = strategy.recommendNow(
                List.of(flexible, fixed), TemporalProfile.defaultFor("u"), List.of(block), now);

        assertTrue(rec.isPresent());
        assertEquals("fixed", rec.get().getTask().getId());
    }

    @Test
    void deepWorkIsPreferredInHighEnergyWindow() {
        // Default profile: best deep-work period = MORNING, morning energy = HIGH.
        TimeBlock morning = new TimeBlock("b", "u", LocalTime.of(9, 0), LocalTime.of(12, 0),
                EnergyLevel.HIGH, 180, "morning");
        Task deep = task("deep", TaskTemporalType.DEEP_WORK, Priority.MEDIUM, 90, null);
        Task flexibleLow = task("flex", TaskTemporalType.FLEXIBLE, Priority.LOW, 15, null);

        Optional<Recommendation> rec = strategy.recommendNow(
                List.of(flexibleLow, deep), TemporalProfile.defaultFor("u"), List.of(morning), now);

        assertTrue(rec.isPresent());
        assertEquals("deep", rec.get().getTask().getId());
    }
}

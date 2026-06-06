package service;

import dao.InMemoryTaskDao;
import model.Priority;
import model.ScheduleBlock;
import model.Task;
import model.TaskStatus;
import model.TaskType;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import strategy.DefaultScoringStrategy;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Schedule is built from real tasks only: empty list → empty schedule. */
class SmartScheduleServiceTest {

    private Session session;
    private TaskService tasks;
    private SmartScheduleService schedule;

    @BeforeEach
    void setUp() {
        session = new Session();
        session.set(new User("u1", "Sam", "sam@example.com", null, 0, null));
        tasks = new TaskServiceImpl(new InMemoryTaskDao(session), new DefaultScoringStrategy());
        schedule = new SmartScheduleService(tasks);
    }

    @Test
    void noTasksProducesEmptySchedule() {
        assertTrue(schedule.generate().isEmpty());
    }

    @Test
    void buildsBlocksFromRealActiveTasksStartingAtNine() {
        tasks.save(new Task("t1", "High value", "", Priority.HIGH, TaskStatus.TODO,
                TaskType.DEEP_WORK, LocalDate.now(), Duration.ofMinutes(60), 0, 0.9));
        tasks.save(new Task("t2", "Low value", "", Priority.LOW, TaskStatus.TODO,
                TaskType.ADMIN, null, Duration.ofMinutes(30), 0, 0.1));

        List<ScheduleBlock> blocks = schedule.generate();
        assertFalse(blocks.isEmpty());
        assertEquals(LocalTime.of(9, 0), blocks.get(0).getStart());
        assertEquals("High value", blocks.get(0).getLabel(), "highest-scoring task is scheduled first");
    }

    @Test
    void completedTasksAreNotScheduled() {
        tasks.save(new Task("t1", "Done already", "", Priority.HIGH, TaskStatus.DONE,
                TaskType.DEEP_WORK, LocalDate.now(), Duration.ofMinutes(60), 0, 0.9));
        assertTrue(schedule.generate().isEmpty());
    }
}

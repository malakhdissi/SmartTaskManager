package service;

import model.Priority;
import model.ScheduleBlock;
import model.Task;
import model.TaskStatus;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * SmartScheduleService — builds a day plan from the user's <em>real</em> active
 * tasks (no invented tasks). Highest-scoring tasks are placed first from 09:00,
 * each block sized by the task's estimated duration, with short breaks between.
 *
 * <p>If there are no active tasks, it returns an empty list so the UI can show
 * a "create tasks to generate your schedule" empty state.</p>
 */
public class SmartScheduleService {

    private static final LocalTime DAY_START = LocalTime.of(9, 0);
    private static final LocalTime DAY_END = LocalTime.of(18, 0);
    private static final int BREAK_MINUTES = 10;
    private static final int MIN_BLOCK = 15;
    private static final int MAX_BLOCK = 120;

    private final TaskService tasks;

    public SmartScheduleService(TaskService tasks) {
        this.tasks = tasks;
    }

    public List<ScheduleBlock> generate() {
        List<Task> active;
        try {
            active = tasks.getAllTasks().stream()
                    .filter(t -> t.getStatus() == TaskStatus.TODO || t.getStatus() == TaskStatus.IN_PROGRESS)
                    .sorted(Comparator.comparingDouble(Task::getScore).reversed())
                    .toList();
        } catch (Exception e) {
            System.err.println("[SmartScheduleService] task fetch failed: " + e);
            return List.of();
        }
        if (active.isEmpty()) return List.of();

        List<ScheduleBlock> blocks = new ArrayList<>();
        LocalTime cursor = DAY_START;
        for (Task t : active) {
            int minutes = clamp((int) (t.getEstimatedDuration() == null ? 30 : t.getEstimatedDuration().toMinutes()));
            LocalTime end = cursor.plusMinutes(minutes);
            if (end.isAfter(DAY_END)) break; // don't overflow the working day
            blocks.add(new ScheduleBlock(cursor, end, t.getTitle(), t.getType(), accentFor(t)));
            cursor = end.plusMinutes(BREAK_MINUTES);
            if (!cursor.isBefore(DAY_END)) break;
        }
        return blocks;
    }

    private static int clamp(int minutes) {
        return Math.max(MIN_BLOCK, Math.min(MAX_BLOCK, minutes));
    }

    private static String accentFor(Task t) {
        if (t.getPriority() == Priority.HIGH) return "warning";
        if (t.getPriority() == Priority.LOW) return "success";
        return "primary";
    }
}

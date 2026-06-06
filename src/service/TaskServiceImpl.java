package service;

import dao.TaskDao;
import model.Task;
import model.TaskStatus;
import strategy.ScoringStrategy;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TaskServiceImpl — bridges the UI with the DAO and the scoring strategy.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Apply the {@link ScoringStrategy} whenever a task is read/saved.</li>
 *   <li>Filter / sort tasks for the dashboard.</li>
 *   <li>Hide DAO details from views.</li>
 * </ul>
 * Dependency injection (DAO + strategy via constructor) keeps everything
 * testable in JUnit later, without spinning up JavaFX.</p>
 */
public class TaskServiceImpl implements TaskService {

    private final TaskDao dao;
    private final ScoringStrategy scoring;

    public TaskServiceImpl(TaskDao dao, ScoringStrategy scoring) {
        this.dao = dao;
        this.scoring = scoring;
    }

    @Override
    public List<Task> getAllTasks() {
        List<Task> all = dao.findAll();
        // Refresh scores on each read so changes in priority/deadline are reflected.
        all.forEach(this::refreshScore);
        return all;
    }

    @Override
    public Optional<Task> getById(String id) {
        return dao.findById(id).map(t -> { refreshScore(t); return t; });
    }

    @Override
    public List<Task> getTopTasks(int n) {
        return getAllTasks().stream()
                .filter(t -> t.getStatus() != TaskStatus.DONE && t.getStatus() != TaskStatus.SKIPPED)
                .sorted(Comparator.comparingDouble(Task::getScore).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    @Override
    public Task save(Task task) {
        refreshScore(task);
        return dao.save(task);
    }

    @Override
    public boolean delete(String id) { return dao.deleteById(id); }

    @Override
    public Optional<Task> markDone(String id) {
        return dao.findById(id).map(t -> {
            t.setStatus(TaskStatus.DONE);
            return dao.save(t);
        });
    }

    /** Pure helper — recomputes score from the strategy and writes it back on the task. */
    private void refreshScore(Task t) {
        t.setScore(scoring.score(t));
    }
}

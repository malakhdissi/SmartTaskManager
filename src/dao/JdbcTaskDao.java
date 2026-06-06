package dao;

import model.Priority;
import model.Task;
import model.TaskStatus;
import model.TaskType;
import service.Session;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JdbcTaskDao — MySQL-backed, per-user task store.
 *
 * <p>Every query is scoped to the currently authenticated user (from
 * {@link Session}). The {@link TaskDao} interface is unchanged, so the service
 * and view layers are completely unaware that tasks are now user-scoped and
 * persistent.</p>
 */
public class JdbcTaskDao implements TaskDao {

    private final Session session;

    public JdbcTaskDao(Session session) { this.session = session; }

    private String userId() {
        String id = session.currentUserId();
        if (id == null) throw new DataAccessException("No authenticated user for task access", null);
        return id;
    }

    @Override
    public List<Task> findAll() {
        String uid = session.currentUserId();
        if (uid == null) return new ArrayList<>(); // not logged in → nothing to show
        String sql = "SELECT * FROM tasks WHERE user_id = ?";
        List<Task> out = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, uid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
            return out;
        } catch (SQLException e) {
            throw new DataAccessException("findAll tasks failed", e);
        }
    }

    @Override
    public Optional<Task> findById(String id) {
        String uid = session.currentUserId();
        if (uid == null) return Optional.empty();
        String sql = "SELECT * FROM tasks WHERE id = ? AND user_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, uid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("findById task failed", e);
        }
    }

    @Override
    public Task save(Task task) {
        String uid = userId();
        String update = "UPDATE tasks SET title=?, description=?, priority=?, status=?, type=?, "
                + "deadline=?, est_minutes=?, score=?, goal_contribution=?, temporal_type=?, goal_id=? WHERE id=? AND user_id=?";
        String insert = "INSERT INTO tasks (id, user_id, title, description, priority, status, type, "
                + "deadline, est_minutes, score, goal_contribution, temporal_type, goal_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = Database.getConnection()) {
            try (PreparedStatement up = c.prepareStatement(update)) {
                up.setString(1, task.getTitle());
                up.setString(2, task.getDescription());
                up.setString(3, name(task.getPriority()));
                up.setString(4, name(task.getStatus()));
                up.setString(5, name(task.getType()));
                up.setDate(6, task.getDeadline() == null ? null : Date.valueOf(task.getDeadline()));
                up.setInt(7, minutes(task.getEstimatedDuration()));
                up.setDouble(8, task.getScore());
                up.setDouble(9, task.getGoalContribution());
                up.setString(10, name(task.getExplicitTemporalType()));
                up.setString(11, task.getGoalId());
                up.setString(12, task.getId());
                up.setString(13, uid);
                if (up.executeUpdate() > 0) return task;
            }
            try (PreparedStatement in = c.prepareStatement(insert)) {
                in.setString(1, task.getId());
                in.setString(2, uid);
                in.setString(3, task.getTitle());
                in.setString(4, task.getDescription());
                in.setString(5, name(task.getPriority()));
                in.setString(6, name(task.getStatus()));
                in.setString(7, name(task.getType()));
                in.setDate(8, task.getDeadline() == null ? null : Date.valueOf(task.getDeadline()));
                in.setInt(9, minutes(task.getEstimatedDuration()));
                in.setDouble(10, task.getScore());
                in.setDouble(11, task.getGoalContribution());
                in.setString(12, name(task.getExplicitTemporalType()));
                in.setString(13, task.getGoalId());
                in.executeUpdate();
            }
            return task;
        } catch (SQLException e) {
            throw new DataAccessException("save task failed", e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        String uid = userId();
        String sql = "DELETE FROM tasks WHERE id = ? AND user_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, uid);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("delete task failed", e);
        }
    }

    /* ---------------- mapping helpers ---------------- */

    private Task map(ResultSet rs) throws SQLException {
        Date d = rs.getDate("deadline");
        LocalDate deadline = d == null ? null : d.toLocalDate();
        Task task = new Task(
                rs.getString("id"),
                rs.getString("title"),
                rs.getString("description"),
                parsePriority(rs.getString("priority")),
                parseStatus(rs.getString("status")),
                parseType(rs.getString("type")),
                deadline,
                Duration.ofMinutes(rs.getInt("est_minutes")),
                rs.getDouble("score"),
                rs.getDouble("goal_contribution"));
        String tt = rs.getString("temporal_type");
        if (tt != null) {
            try { task.setTemporalType(model.TaskTemporalType.valueOf(tt)); }
            catch (IllegalArgumentException ignored) { }
        }
        task.setGoalId(rs.getString("goal_id"));
        return task;
    }

    private static String name(Enum<?> e) { return e == null ? null : e.name(); }
    private static int minutes(Duration d) { return d == null ? 30 : (int) d.toMinutes(); }

    private static Priority parsePriority(String s) {
        try { return s == null ? Priority.MEDIUM : Priority.valueOf(s); }
        catch (IllegalArgumentException e) { return Priority.MEDIUM; }
    }
    private static TaskStatus parseStatus(String s) {
        try { return s == null ? TaskStatus.TODO : TaskStatus.valueOf(s); }
        catch (IllegalArgumentException e) { return TaskStatus.TODO; }
    }
    private static TaskType parseType(String s) {
        try { return s == null ? TaskType.OTHER : TaskType.valueOf(s); }
        catch (IllegalArgumentException e) { return TaskType.OTHER; }
    }
}

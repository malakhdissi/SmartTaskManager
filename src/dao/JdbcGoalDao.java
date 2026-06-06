package dao;

import model.Goal;
import model.GoalCategory;
import model.GoalStatus;
import service.Session;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** MySQL-backed, per-user goal store. */
public class JdbcGoalDao implements GoalDao {

    private static final String COLS = "id, title, description, progress, active, category, importance, target_date, status";

    private final Session session;

    public JdbcGoalDao(Session session) { this.session = session; }

    private String userId() {
        String id = session.currentUserId();
        if (id == null) throw new DataAccessException("No authenticated user for goal access", null);
        return id;
    }

    @Override
    public List<Goal> findAll() {
        String uid = session.currentUserId();
        if (uid == null) return new ArrayList<>();
        List<Goal> out = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLS + " FROM goals WHERE user_id = ?")) {
            ps.setString(1, uid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
            return out;
        } catch (SQLException e) {
            throw new DataAccessException("findAll goals failed", e);
        }
    }

    @Override
    public Optional<Goal> findById(String id) {
        String uid = session.currentUserId();
        if (uid == null) return Optional.empty();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLS + " FROM goals WHERE id = ? AND user_id = ?")) {
            ps.setString(1, id);
            ps.setString(2, uid);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("findById goal failed", e);
        }
    }

    @Override
    public Optional<Goal> findActive() {
        String uid = session.currentUserId();
        if (uid == null) return Optional.empty();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT " + COLS + " FROM goals WHERE user_id = ? AND active = TRUE LIMIT 1")) {
            ps.setString(1, uid);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("findActive goal failed", e);
        }
    }

    @Override
    public Goal save(Goal goal) {
        String uid = userId();
        String update = "UPDATE goals SET title=?, description=?, progress=?, active=?, category=?, importance=?, "
                + "target_date=?, status=?, updated_at=CURRENT_TIMESTAMP WHERE id=? AND user_id=?";
        String insert = "INSERT INTO goals (id, user_id, title, description, progress, active, category, importance, "
                + "target_date, status) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = Database.getConnection()) {
            try (PreparedStatement up = c.prepareStatement(update)) {
                up.setString(1, goal.getTitle());
                up.setString(2, goal.getDescription());
                up.setDouble(3, goal.getProgress());
                up.setBoolean(4, goal.isActive());
                up.setString(5, goal.getCategory() == null ? null : goal.getCategory().name());
                up.setInt(6, goal.getImportance());
                up.setDate(7, goal.getTargetDate() == null ? null : Date.valueOf(goal.getTargetDate()));
                up.setString(8, goal.getStatus() == null ? GoalStatus.ACTIVE.name() : goal.getStatus().name());
                up.setString(9, goal.getId());
                up.setString(10, uid);
                if (up.executeUpdate() > 0) return goal;
            }
            try (PreparedStatement in = c.prepareStatement(insert)) {
                in.setString(1, goal.getId());
                in.setString(2, uid);
                in.setString(3, goal.getTitle());
                in.setString(4, goal.getDescription());
                in.setDouble(5, goal.getProgress());
                in.setBoolean(6, goal.isActive());
                in.setString(7, goal.getCategory() == null ? null : goal.getCategory().name());
                in.setInt(8, goal.getImportance());
                in.setDate(9, goal.getTargetDate() == null ? null : Date.valueOf(goal.getTargetDate()));
                in.setString(10, goal.getStatus() == null ? GoalStatus.ACTIVE.name() : goal.getStatus().name());
                in.executeUpdate();
            }
            return goal;
        } catch (SQLException e) {
            throw new DataAccessException("save goal failed", e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        String uid = userId();
        try (Connection c = Database.getConnection()) {
            // Unlink tasks from the goal first (app-managed integrity; no FK on tasks.goal_id).
            try (PreparedStatement un = c.prepareStatement("UPDATE tasks SET goal_id = NULL WHERE goal_id = ? AND user_id = ?")) {
                un.setString(1, id);
                un.setString(2, uid);
                un.executeUpdate();
            }
            try (PreparedStatement del = c.prepareStatement("DELETE FROM goals WHERE id = ? AND user_id = ?")) {
                del.setString(1, id);
                del.setString(2, uid);
                return del.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("delete goal failed", e);
        }
    }

    @Override
    public void setActive(String id) {
        String uid = userId();
        try (Connection c = Database.getConnection()) {
            try (PreparedStatement clear = c.prepareStatement("UPDATE goals SET active = FALSE WHERE user_id = ?")) {
                clear.setString(1, uid);
                clear.executeUpdate();
            }
            try (PreparedStatement set = c.prepareStatement("UPDATE goals SET active = TRUE WHERE id = ? AND user_id = ?")) {
                set.setString(1, id);
                set.setString(2, uid);
                set.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException("setActive goal failed", e);
        }
    }

    private Goal map(ResultSet rs) throws SQLException {
        Date d = rs.getDate("target_date");
        return new Goal(
                rs.getString("id"),
                rs.getString("title"),
                rs.getString("description"),
                category(rs.getString("category")),
                rs.getInt("importance"),
                d == null ? null : d.toLocalDate(),
                status(rs.getString("status")),
                rs.getDouble("progress"),
                rs.getBoolean("active"));
    }

    private static GoalCategory category(String s) {
        try { return s == null ? GoalCategory.OTHER : GoalCategory.valueOf(s); }
        catch (IllegalArgumentException e) { return GoalCategory.OTHER; }
    }

    private static GoalStatus status(String s) {
        try { return s == null ? GoalStatus.ACTIVE : GoalStatus.valueOf(s); }
        catch (IllegalArgumentException e) { return GoalStatus.ACTIVE; }
    }
}

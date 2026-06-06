package dao;

import model.EnergyLevel;
import model.TimeBlock;
import service.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/** MySQL-backed, per-user {@link TimeBlockDAO}. */
public class JdbcTimeBlockDAO implements TimeBlockDAO {

    private final Session session;

    public JdbcTimeBlockDAO(Session session) { this.session = session; }

    private String userId() {
        String id = session.currentUserId();
        if (id == null) throw new DataAccessException("No authenticated user for time-block access", null);
        return id;
    }

    @Override
    public List<TimeBlock> findForCurrentUser() {
        String uid = session.currentUserId();
        if (uid == null) return new ArrayList<>();
        String sql = "SELECT * FROM time_blocks WHERE user_id = ? ORDER BY start_time";
        List<TimeBlock> out = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, uid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
            return out;
        } catch (SQLException e) {
            throw new DataAccessException("findForCurrentUser time blocks failed", e);
        }
    }

    @Override
    public TimeBlock save(TimeBlock block) {
        String uid = userId();
        String update = "UPDATE time_blocks SET start_time=?, end_time=?, energy_level=?, "
                + "available_minutes=?, label=? WHERE id=? AND user_id=?";
        String insert = "INSERT INTO time_blocks (id, user_id, start_time, end_time, energy_level, "
                + "available_minutes, label) VALUES (?,?,?,?,?,?,?)";
        try (Connection c = Database.getConnection()) {
            try (PreparedStatement up = c.prepareStatement(update)) {
                up.setTime(1, Time.valueOf(block.getStartTime()));
                up.setTime(2, Time.valueOf(block.getEndTime()));
                up.setString(3, block.getEnergyLevel().name());
                up.setInt(4, block.getAvailableMinutes());
                up.setString(5, block.getLabel());
                up.setString(6, block.getId());
                up.setString(7, uid);
                if (up.executeUpdate() > 0) return block;
            }
            try (PreparedStatement in = c.prepareStatement(insert)) {
                in.setString(1, block.getId());
                in.setString(2, uid);
                in.setTime(3, Time.valueOf(block.getStartTime()));
                in.setTime(4, Time.valueOf(block.getEndTime()));
                in.setString(5, block.getEnergyLevel().name());
                in.setInt(6, block.getAvailableMinutes());
                in.setString(7, block.getLabel());
                in.executeUpdate();
            }
            return block;
        } catch (SQLException e) {
            throw new DataAccessException("save time block failed", e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        String uid = userId();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM time_blocks WHERE id=? AND user_id=?")) {
            ps.setString(1, id);
            ps.setString(2, uid);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("delete time block failed", e);
        }
    }

    private TimeBlock map(ResultSet rs) throws SQLException {
        EnergyLevel energy;
        try { energy = EnergyLevel.valueOf(rs.getString("energy_level")); }
        catch (IllegalArgumentException e) { energy = EnergyLevel.MEDIUM; }
        return new TimeBlock(
                rs.getString("id"),
                rs.getString("user_id"),
                rs.getTime("start_time").toLocalTime(),
                rs.getTime("end_time").toLocalTime(),
                energy,
                rs.getInt("available_minutes"),
                rs.getString("label"));
    }
}

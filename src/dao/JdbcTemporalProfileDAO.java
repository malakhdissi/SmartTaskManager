package dao;

import model.Chronotype;
import model.DayPeriod;
import model.EnergyLevel;
import model.TemporalProfile;
import service.Session;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/** MySQL-backed, per-user {@link TemporalProfileDAO} (PK = user_id). */
public class JdbcTemporalProfileDAO implements TemporalProfileDAO {

    private final Session session;

    public JdbcTemporalProfileDAO(Session session) { this.session = session; }

    private String userId() {
        String id = session.currentUserId();
        if (id == null) throw new DataAccessException("No authenticated user for temporal profile", null);
        return id;
    }

    @Override
    public Optional<TemporalProfile> findForCurrentUser() {
        String uid = session.currentUserId();
        if (uid == null) return Optional.empty();
        String sql = "SELECT * FROM temporal_profiles WHERE user_id = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, uid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("findForCurrentUser temporal profile failed", e);
        }
    }

    @Override
    public TemporalProfile save(TemporalProfile profile) {
        String uid = userId();
        String update = "UPDATE temporal_profiles SET morning_energy=?, afternoon_energy=?, evening_energy=?, "
                + "chronotype=?, best_deep_work_period=?, fatigue_period=? WHERE user_id=?";
        String insert = "INSERT INTO temporal_profiles (user_id, morning_energy, afternoon_energy, evening_energy, "
                + "chronotype, best_deep_work_period, fatigue_period) VALUES (?,?,?,?,?,?,?)";
        try (Connection c = Database.getConnection()) {
            try (PreparedStatement up = c.prepareStatement(update)) {
                up.setString(1, profile.getPreferredMorningEnergy().name());
                up.setString(2, profile.getPreferredAfternoonEnergy().name());
                up.setString(3, profile.getPreferredEveningEnergy().name());
                up.setString(4, profile.getChronotype().name());
                up.setString(5, profile.getBestDeepWorkPeriod().name());
                up.setString(6, profile.getFatiguePeriod().name());
                up.setString(7, uid);
                if (up.executeUpdate() > 0) return profile;
            }
            try (PreparedStatement in = c.prepareStatement(insert)) {
                in.setString(1, uid);
                in.setString(2, profile.getPreferredMorningEnergy().name());
                in.setString(3, profile.getPreferredAfternoonEnergy().name());
                in.setString(4, profile.getPreferredEveningEnergy().name());
                in.setString(5, profile.getChronotype().name());
                in.setString(6, profile.getBestDeepWorkPeriod().name());
                in.setString(7, profile.getFatiguePeriod().name());
                in.executeUpdate();
            }
            return profile;
        } catch (SQLException e) {
            throw new DataAccessException("save temporal profile failed", e);
        }
    }

    private TemporalProfile map(ResultSet rs) throws SQLException {
        return new TemporalProfile(
                rs.getString("user_id"),
                energy(rs.getString("morning_energy")),
                energy(rs.getString("afternoon_energy")),
                energy(rs.getString("evening_energy")),
                chronotype(rs.getString("chronotype")),
                period(rs.getString("best_deep_work_period")),
                period(rs.getString("fatigue_period")));
    }

    private static EnergyLevel energy(String s) {
        try { return EnergyLevel.valueOf(s); } catch (Exception e) { return EnergyLevel.MEDIUM; }
    }
    private static Chronotype chronotype(String s) {
        try { return Chronotype.valueOf(s); } catch (Exception e) { return Chronotype.INTERMEDIATE; }
    }
    private static DayPeriod period(String s) {
        try { return DayPeriod.valueOf(s); } catch (Exception e) { return DayPeriod.MORNING; }
    }
}

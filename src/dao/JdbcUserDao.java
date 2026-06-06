package dao;

import model.Persona;
import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * JdbcUserDao — MySQL-backed user store.
 *
 * <p>All statements are parameterized (no string concatenation) to avoid SQL
 * injection. Email normalization (trim/lower-case) is the caller's job
 * ({@link service.AuthService}); this DAO matches exactly what it is given.</p>
 */
public class JdbcUserDao implements UserDao {

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT id, display_name, email, password_hash, streak_days, persona, is_guest FROM users WHERE email = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException("findByEmail failed", e);
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        String sql = "SELECT 1 FROM users WHERE email = ?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException("existsByEmail failed", e);
        }
    }

    @Override
    public User save(User user) {
        // Upsert by primary key: update if present, otherwise insert.
        String update = "UPDATE users SET display_name=?, email=?, password_hash=?, streak_days=?, persona=? WHERE id=?";
        String insert = "INSERT INTO users (id, display_name, email, password_hash, streak_days, persona, is_guest) "
                + "VALUES (?,?,?,?,?,?,?)";
        try (Connection c = Database.getConnection()) {
            try (PreparedStatement up = c.prepareStatement(update)) {
                up.setString(1, user.getDisplayName());
                up.setString(2, user.getEmail());
                up.setString(3, user.getPasswordHash());
                up.setInt(4, user.getCurrentStreakDays());
                up.setString(5, user.getPersona() == null ? null : user.getPersona().name());
                up.setString(6, user.getId());
                if (up.executeUpdate() > 0) return user;
            }
            try (PreparedStatement in = c.prepareStatement(insert)) {
                in.setString(1, user.getId());
                in.setString(2, user.getDisplayName());
                in.setString(3, user.getEmail());
                in.setString(4, user.getPasswordHash());
                in.setInt(5, user.getCurrentStreakDays());
                in.setString(6, user.getPersona() == null ? null : user.getPersona().name());
                in.setBoolean(7, user.isGuest());
                in.executeUpdate();
            }
            return user;
        } catch (SQLException e) {
            throw new DataAccessException("save user failed", e);
        }
    }

    private User map(ResultSet rs) throws SQLException {
        Persona persona = null;
        String p = rs.getString("persona");
        if (p != null) {
            try { persona = Persona.valueOf(p); } catch (IllegalArgumentException ignored) { }
        }
        User user = new User(
                rs.getString("id"),
                rs.getString("display_name"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getInt("streak_days"),
                persona);
        user.setGuest(rs.getBoolean("is_guest"));
        return user;
    }
}

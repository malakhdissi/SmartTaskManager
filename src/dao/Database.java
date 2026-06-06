package dao;

import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Database — owns JDBC connectivity and one-time schema bootstrap.
 *
 * <p>Deliberately small: reads {@code /db.properties} from the classpath,
 * hands out short-lived {@link Connection}s via {@link DriverManager}, and
 * applies {@code /db/schema.sql} on {@link #init()}. No pooling library is
 * used — for a single-user desktop app the connection cost is negligible and
 * fewer dependencies means fewer ways to break.</p>
 *
 * <p>If the database cannot be reached, {@link #init()} returns {@code false}
 * and the application wires in-memory DAOs instead, so the UI still launches.</p>
 */
public final class Database {

    private static String url;
    private static String user;
    private static String password;
    private static boolean ready = false;

    private Database() {}

    /**
     * Loads config, verifies connectivity, and applies the schema.
     * @return true if the database is usable; false to trigger in-memory fallback.
     */
    public static synchronized boolean init() {
        try {
            Properties props = new Properties();
            try (InputStream in = Database.class.getResourceAsStream("/db.properties")) {
                if (in == null) {
                    System.err.println("[Database] /db.properties not found on classpath — using in-memory storage.");
                    return false;
                }
                props.load(in);
            }
            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password", "");

            // Verify we can actually connect before committing to JDBC DAOs.
            try (Connection c = DriverManager.getConnection(url, user, password)) {
                applySchema(c);
                migrate(c);
            }
            ready = true;
            System.out.println("[Database] Connected. Persistence is online.");
            return true;
        } catch (Exception e) {
            System.err.println("[Database] Unavailable (" + e.getMessage()
                    + ") — falling back to in-memory storage. Data will not persist.");
            ready = false;
            return false;
        }
    }

    /** True once {@link #init()} has succeeded. */
    public static boolean isReady() { return ready; }

    /** A fresh connection. Callers must close it (use try-with-resources). */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Best-effort, idempotent column migrations for databases created by an
     * earlier version. Each statement is tried independently and a failure
     * (e.g. "column already exists") is logged and ignored — so this is safe to
     * run on every startup, on both fresh and existing schemas.
     */
    private static void migrate(Connection c) {
        String[] migrations = {
                "ALTER TABLE tasks ADD COLUMN temporal_type VARCHAR(20)",
                "ALTER TABLE tasks ADD COLUMN goal_id VARCHAR(36)",
                "ALTER TABLE goals ADD COLUMN category VARCHAR(20)",
                "ALTER TABLE goals ADD COLUMN importance INT NOT NULL DEFAULT 3",
                "ALTER TABLE goals ADD COLUMN target_date DATE",
                "ALTER TABLE goals ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'",
                "ALTER TABLE goals ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP",
                "ALTER TABLE goals ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
        };
        for (String m : migrations) {
            try (Statement st = c.createStatement()) {
                st.execute(m);
                System.out.println("[Database] migration applied: " + m);
            } catch (SQLException ignored) {
                // Column already present (or similar) — expected on existing DBs.
            }
        }
    }

    /** Reads schema.sql from the classpath and runs each statement. */
    private static void applySchema(Connection c) throws SQLException {
        String sql;
        try (InputStream in = Database.class.getResourceAsStream("/db/schema.sql")) {
            if (in == null) throw new SQLException("/db/schema.sql not found on classpath");
            sql = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new SQLException("Could not read schema.sql: " + e.getMessage(), e);
        }
        // Strip line comments first, otherwise a leading "--" header would be
        // glued onto the first statement after splitting on ';'.
        StringBuilder clean = new StringBuilder();
        for (String line : sql.split("\n")) {
            String l = line.strip();
            if (l.isEmpty() || l.startsWith("--")) continue;
            clean.append(line).append('\n');
        }
        try (Statement st = c.createStatement()) {
            for (String stmt : clean.toString().split(";")) {
                String trimmed = stmt.trim();
                if (trimmed.isEmpty()) continue;
                st.execute(trimmed);
            }
        }
    }
}

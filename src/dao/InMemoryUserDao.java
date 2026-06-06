package dao;

import model.User;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * InMemoryUserDao — non-persistent fallback used when the database is
 * unreachable, and for unit tests.
 *
 * <p>Unlike the old MVP version, this seeds <em>no</em> mock user: a fresh
 * store starts empty, so accounts only exist after a real signup. Data is lost
 * on restart — that is the deliberate, honest signal of offline mode.</p>
 */
public class InMemoryUserDao implements UserDao {

    /** Keyed by normalized email for O(1) login lookups. */
    private final Map<String, User> byEmail = new ConcurrentHashMap<>();

    @Override
    public Optional<User> findByEmail(String email) {
        return Optional.ofNullable(byEmail.get(email));
    }

    @Override
    public boolean existsByEmail(String email) {
        return byEmail.containsKey(email);
    }

    @Override
    public User save(User user) {
        byEmail.put(user.getEmail(), user);
        return user;
    }
}

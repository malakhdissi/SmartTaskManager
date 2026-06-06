package service;

import dao.UserDao;
import model.User;

import java.util.Optional;

/**
 * UserService — read/update of the authenticated user profile.
 *
 * <p>"Current user" now comes from the {@link Session} (set at login), not from
 * the DAO. Profile edits are persisted through {@link UserDao} and reflected
 * back into the session.</p>
 */
public class UserService {

    private final Session session;
    private final UserDao dao;

    public UserService(Session session, UserDao dao) {
        this.session = session;
        this.dao = dao;
    }

    public Optional<User> getCurrentUser() { return session.currentUser(); }

    public User update(User user) { return dao.save(user); }
}

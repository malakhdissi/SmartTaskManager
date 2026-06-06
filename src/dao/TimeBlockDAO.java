package dao;

import model.TimeBlock;

import java.util.List;

/**
 * TimeBlockDAO — per-user persistence for the user's daily time blocks.
 * Scoped to the authenticated user via {@link service.Session}.
 */
public interface TimeBlockDAO {
    List<TimeBlock> findForCurrentUser();
    TimeBlock save(TimeBlock block);
    boolean deleteById(String id);
}

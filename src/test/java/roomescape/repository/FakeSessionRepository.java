package roomescape.repository;

import roomescape.domain.Session;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FakeSessionRepository implements SessionRepository {

    private final Map<Long, Session> storage = new HashMap<>();
    private long sequence = 1L;

    @Override
    public Session save(Session session) {
        long id = sequence++;
        Session savedSession = new Session(id, session.getDate(), session.getTimeSlot(), session.getTheme());
        storage.put(id, savedSession);
        return savedSession;
    }

    @Override
    public Optional<Session> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Session> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        return storage.values().stream()
                .filter(session -> matchCondition(session, date, timeId, themeId))
                .findAny();
    }

    @Override
    public void deleteById(long id) {
        storage.remove(id);
    }

    @Override
    public List<Session> findAll() {
        return List.copyOf(storage.values());
    }

    private boolean matchCondition(Session session, LocalDate date, Long timeId, Long themeId) {
        return session.getDate().equals(date)
                && session.getTimeSlot().getId().equals(timeId)
                && session.getTheme().getId().equals(themeId);
    }
}

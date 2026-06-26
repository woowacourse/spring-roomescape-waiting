package roomescape.repository;

import java.time.LocalDate;
import java.util.Optional;
import roomescape.domain.Session;

public class FakeSessionRepository extends AbstractFakeRepository<Session, Long> implements SessionRepository {

    @Override
    protected Long getId(Session entity) {
        return entity.getId();
    }

    @Override
    protected Session withId(Session entity, Long id) {
        return new Session(id, entity.getDate(), entity.getTimeSlot(), entity.getTheme());
    }

    @Override
    public Optional<Session> findByDateAndTimeSlotIdAndThemeId(LocalDate date, Long timeSlotId, Long themeId) {
        return store.values().stream()
                .filter(s -> s.getDate().equals(date)
                        && s.getTimeSlot().getId().equals(timeSlotId)
                        && s.getTheme().getId().equals(themeId))
                .findFirst();
    }
}

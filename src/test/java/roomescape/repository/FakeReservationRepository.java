package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.Reservation;
import roomescape.domain.Session;

public class FakeReservationRepository extends AbstractFakeRepository<Reservation, Long> implements ReservationRepository {

    @Override
    protected Long getId(Reservation entity) {
        return entity.getId();
    }

    @Override
    protected Reservation withId(Reservation entity, Long id) {
        return new Reservation(id, entity.getName(), entity.getSession());
    }

    @Override
    public List<Reservation> findByName(String name) {
        return store.values().stream()
                .filter(r -> r.getName().equals(name))
                .toList();
    }

    @Override
    public Optional<Reservation> findBySession(Session session) {
        return store.values().stream()
                .filter(r -> r.getSession().getId().equals(session.getId()))
                .findFirst();
    }

    @Override
    public Optional<Reservation> findBySessionId(Long sessionId) {
        return store.values().stream()
                .filter(r -> r.getSession().getId().equals(sessionId))
                .findFirst();
    }
}

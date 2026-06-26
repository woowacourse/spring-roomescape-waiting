package roomescape.repository;

import java.util.Comparator;
import java.util.List;
import roomescape.domain.Session;
import roomescape.domain.Waiting;

public class FakeWaitingRepository extends AbstractFakeRepository<Waiting, Long> implements WaitingRepository {

    @Override
    protected Long getId(Waiting entity) {
        return entity.getId();
    }

    @Override
    protected Waiting withId(Waiting entity, Long id) {
        return new Waiting(id, entity.getName(), entity.getSession(), entity.getWaitingNumber());
    }

    @Override
    public List<Waiting> findByName(String name) {
        return store.values().stream()
                .filter(w -> w.getName().equals(name))
                .toList();
    }

    @Override
    public boolean existsByNameAndSession(String name, Session session) {
        return store.values().stream()
                .anyMatch(w -> w.getName().equals(name) && w.getSession().getId().equals(session.getId()));
    }

    @Override
    public boolean existsBySession(Session session) {
        return store.values().stream()
                .anyMatch(w -> w.getSession().getId().equals(session.getId()));
    }

    @Override
    public Waiting findFirstBySessionOrderByIdAsc(Session session) {
        return store.values().stream()
                .filter(w -> w.getSession().getId().equals(session.getId()))
                .min(Comparator.comparingLong(Waiting::getId))
                .orElse(null);
    }

    @Override
    public List<Waiting> findBySessionOrderByIdAsc(Session session) {
        return store.values().stream()
                .filter(w -> w.getSession().getId().equals(session.getId()))
                .sorted(Comparator.comparingLong(Waiting::getId))
                .toList();
    }

    public boolean isExistsBySessionId(Long sessionId) {
        return store.values().stream()
                .anyMatch(w -> w.getSession().getId().equals(sessionId));
    }
}

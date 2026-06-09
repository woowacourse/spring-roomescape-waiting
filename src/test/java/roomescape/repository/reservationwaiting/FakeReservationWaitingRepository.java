package roomescape.repository.reservationwaiting;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.reservationwaiting.ReservationWaiting;

public class FakeReservationWaitingRepository implements ReservationWaitingRepository {

    private final Map<Long, ReservationWaiting> store = new LinkedHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    public ReservationWaiting save(final ReservationWaiting reservationWaiting) {
        ReservationWaiting saved = reservationWaiting.withId(sequence.incrementAndGet());
        store.put(saved.getId(), saved);
        return saved;
    }

    @Override
    public int deleteByIdAndName(final Long id, final String name) {
        ReservationWaiting found = store.get(id);
        if (found == null || !found.getName().equals(name)) {
            return 0;
        }
        store.remove(id);
        return 1;
    }

    @Override
    public int deleteById(final long id) {
        return store.remove(id) != null ? 1 : 0;
    }

    @Override
    public Optional<ReservationWaiting> findEarliestByReservationId(final long reservationId) {
        return store.values().stream()
                .filter(waiting -> waiting.getReservation().getId().equals(reservationId))
                .min(Comparator.comparing(ReservationWaiting::getRequestedAt)
                        .thenComparing(ReservationWaiting::getId));
    }

    @Override
    public boolean existsByReservationIdAndName(final Long reservationId, final String name) {
        return store.values().stream()
                .anyMatch(waiting -> waiting.getReservation().getId().equals(reservationId)
                        && waiting.getName().equals(name));
    }

    @Override
    public boolean existsByReservationId(final Long reservationId) {
        return store.values().stream()
                .anyMatch(waiting -> waiting.getReservation().getId().equals(reservationId));
    }
}

package roomescape.fake;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.reservationWaiting.ReservationWaiting;
import roomescape.repository.ReservationWaitingDao;

public class FakeReservationWaitingDao extends ReservationWaitingDao {

    private final List<ReservationWaiting> store = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public FakeReservationWaitingDao() {
        super(null);
    }

    @Override
    public boolean isExistByNameAndSlotId(String name, Long slotId) {
        return store.stream().anyMatch(w ->
                w.getName().equals(name)
                && w.getSlot().getId().equals(slotId));
    }

    @Override
    public Optional<ReservationWaiting> findFirstBySlotId(Long slotId) {
        return store.stream()
                .filter(w -> w.getSlot().getId().equals(slotId))
                .min(Comparator.comparing(ReservationWaiting::getCreatedAt)
                        .thenComparing(w -> w.getId() == null ? Long.MAX_VALUE : w.getId()))
                .map(this::withSequence);
    }

    @Override
    public Optional<ReservationWaiting> findReservationWaitingById(long id) {
        return store.stream()
                .filter(w -> w.getId() != null && w.getId().equals(id))
                .findFirst()
                .map(this::withSequence);
    }

    @Override
    public List<ReservationWaiting> findAllReservationWaiting() {
        return store.stream().map(this::withSequence).toList();
    }

    @Override
    public List<ReservationWaiting> findAllByName(String name) {
        return store.stream()
                .filter(w -> w.getName().equals(name))
                .map(this::withSequence)
                .toList();
    }

    @Override
    public Long create(ReservationWaiting waiting) {
        Long id = idGenerator.getAndIncrement();
        ReservationWaiting withId = ReservationWaiting.restore(
                id, waiting.getSlot(), waiting.getName(),
                waiting.getSequence(), waiting.getCreatedAt());
        store.add(withId);
        return id;
    }

    @Override
    public void delete(Long id) {
        store.removeIf(w -> w.getId() != null && w.getId().equals(id));
    }

    private ReservationWaiting withSequence(ReservationWaiting waiting) {
        long sequence = store.stream()
                .filter(w -> w.getSlot().getId().equals(waiting.getSlot().getId()))
                .sorted(Comparator.comparing(ReservationWaiting::getCreatedAt)
                        .thenComparing(w -> w.getId() == null ? Long.MAX_VALUE : w.getId()))
                .takeWhile(w -> !w.equals(waiting))
                .count() + 1;

        return ReservationWaiting.restore(
                waiting.getId(), waiting.getSlot(), waiting.getName(),
                sequence, waiting.getCreatedAt());
    }
}

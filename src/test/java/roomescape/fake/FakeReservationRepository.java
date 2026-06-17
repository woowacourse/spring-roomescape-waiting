package roomescape.fake;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;

public class FakeReservationRepository implements ReservationRepository {

    private final List<Reservation> store = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public void save(Reservation reservation) {
        store.add(reservation);
    }

    @Override
    public Optional<Reservation> findReservationById(long id) {
        return store.stream()
                .filter(reservation -> reservation.getId() != null && reservation.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Reservation> findReservationBySlotId(Long slotId) {
        return store.stream()
                .filter(reservation -> reservation.getSlot().getId().equals(slotId))
                .findFirst();
    }

    @Override
    public List<Reservation> findAllReservations() {
        return List.copyOf(store);
    }

    @Override
    public List<Reservation> findAllByName(String name) {
        return store.stream()
                .filter(reservation -> reservation.getName().equals(name))
                .toList();
    }

    @Override
    public boolean isExistBySlot(long slotId) {
        return store.stream().anyMatch(reservation -> reservation.getSlot().getId().equals(slotId));
    }

    @Override
    public Long insert(Reservation reservation) {
        long id = idGenerator.getAndIncrement();
        store.add(reservation.withId(id));
        return id;
    }

    @Override
    public void updateName(Long id, String name) {
        for (int i = 0; i < store.size(); i++) {
            Reservation r = store.get(i);
            if (r.getId() != null && r.getId().equals(id)) {
                store.set(i, Reservation.restore(id, r.getSlot(), name, r.getCreatedAt(), r.isPaid()));
                return;
            }
        }
    }

    @Override
    public long update(Long id, String name, Long slotId, LocalDateTime createdAt) {
        for (int i = 0; i < store.size(); i++) {
            Reservation r = store.get(i);
            if (r.getId() != null && r.getId().equals(id)) {
                store.set(i, Reservation.restore(id, r.getSlot(), name, createdAt, r.isPaid()));
                return 1;
            }
        }
        return 0;
    }

    @Override
    public int updatePaid(Long id, boolean paid) {
        for (int i = 0; i < store.size(); i++) {
            Reservation r = store.get(i);
            if (r.getId() != null && r.getId().equals(id)) {
                store.set(i, r.updatePaid(paid));
                return 1;
            }
        }
        return 0;
    }

    @Override
    public List<Reservation> findUnpaidCreatedBefore(LocalDateTime threshold) {
        return store.stream()
                .filter(reservation -> !reservation.isPaid())
                .filter(reservation -> reservation.getCreatedAt().isBefore(threshold))
                .toList();
    }

    @Override
    public int deleteUnpaidByIds(List<Long> ids) {
        int before = store.size();
        store.removeIf(reservation -> reservation.getId() != null
                && ids.contains(reservation.getId())
                && !reservation.isPaid());
        return before - store.size();
    }

    @Override
    public long delete(Long id) {
        int before = store.size();
        store.removeIf(reservation -> reservation.getId() != null && reservation.getId().equals(id));
        return before - store.size();
    }
}

package roomescape.reservation.fixture;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.dto.ReservationWithSlotInformation;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import static roomescape.reservation.domain.ReservationStatus.PENDING_PAYMENT;
import static roomescape.reservation.domain.ReservationStatus.RESERVED;
import static roomescape.reservation.domain.ReservationStatus.WAITING;

public class FakeReservationRepository implements ReservationRepository {

    private final Map<Long, Reservation> store = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Optional<Reservation> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<ReservationWithSlotInformation> findAll() {
        return store.values().stream()
                .map(r -> new ReservationWithSlotInformation(
                        r.getId(), r.getSlotId(), r.getName(),
                        null, null, null, null, null,
                        r.getStatus(), LocalDateTime.now(), null))
                .toList();
    }

    @Override
    public List<ReservationWithSlotInformation> findByMemberName(String name) {
        return List.of();
    }

    @Override
    public List<Reservation> findReservedAndWaitingBySlotId(Long slotId) {
        return store.values().stream()
                .filter(r -> r.getSlotId() != null && r.getSlotId().equals(slotId))
                .filter(r -> r.getStatus() == RESERVED || r.getStatus() == WAITING || r.getStatus() == PENDING_PAYMENT)
                .sorted(Comparator.comparing(Reservation::getReservedAt))
                .toList();
    }

    @Override
    public Reservation save(Reservation reservation) {
        Long id = idGenerator.getAndIncrement();
        Reservation saved = Reservation.load(id, reservation.getName(), reservation.getSlotId(),
                reservation.getStatus(), reservation.getReservedAt());
        store.put(id, saved);
        return saved;
    }

    public List<Reservation> saveAll(List<Reservation> reservations) {
        List<Reservation> saved = new ArrayList<>();
        for (Reservation r : reservations) {
            saved.add(save(r));
        }
        return saved;
    }

    @Override
    public boolean updateStatus(Reservation reservation) {
        Reservation found = store.get(reservation.getId());
        if (found == null) {
            return false;
        }
        found.updateStatus(reservation.getStatus());
        return true;
    }

    @Override
    public boolean updateSchedule(Reservation reservation) {
        if (!store.containsKey(reservation.getId())) {
            return false;
        }
        store.put(reservation.getId(), reservation);
        return true;
    }

}

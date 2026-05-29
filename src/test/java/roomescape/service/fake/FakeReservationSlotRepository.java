package roomescape.service.fake;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.ReservationSlot;
import roomescape.domain.Reservation;
import roomescape.repository.ReservationSlotRepository;
import roomescape.repository.dto.ReservationCondition;

public class FakeReservationSlotRepository implements ReservationSlotRepository {

    private final Map<Long, ReservationSlot> storage = new HashMap<>();
    private final AtomicLong slotCounter = new AtomicLong(1);
    private final AtomicLong reservationCounter = new AtomicLong(1);

    @Override
    public ReservationSlot save(ReservationSlot slot) {
        Long id = slot.getId() == null ? slotCounter.getAndIncrement() : slot.getId();
        ReservationSlot saved = new ReservationSlot(
                id,
                slot.getDate(),
                slot.getTheme(),
                slot.getTime(),
                copyReservationsWithId(slot)
        );
        storage.put(saved.getId(), saved);
        return saved;
    }

    private List<Reservation> copyReservationsWithId(ReservationSlot slot) {
        return slot.getReservations()
                .stream()
                .map(reservation -> new Reservation(
                        reservation.getId() == null ? reservationCounter.getAndIncrement() : reservation.getId(),
                        reservation.getName(),
                        reservation.getSlot(),
                        reservation.getStatus(),
                        reservation.getCreatedAt()
                ))
                .toList();
    }

    @Override
    public Optional<ReservationSlot> findByDateAndThemeAndTimeForUpdate(ReservationCondition condition) {
        return storage.values()
                .stream()
                .filter(value -> value.getDate().equals(condition.date()) &&
                        value.getTheme().getId().equals(condition.themeId()) &&
                        value.getTime().getId().equals(condition.timeId()))
                .findFirst();
    }

    @Override
    public Optional<ReservationSlot> findByReservationIdForUpdate(long reservationId) {
        return storage.values()
                .stream()
                .filter(slot -> slot.getReservations()
                        .stream()
                        .anyMatch(reservation -> reservation.getId() != null && reservation.isSameId(reservationId)))
                .findFirst();
    }
}

package roomescape.repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.ReservationSlotRepository;

public class FakeReservationSlotRepository implements ReservationSlotRepository {

    private final Map<Long, ReservationSlot> storage = new HashMap<>();
    private long sequence = 1L;

    @Override
    public Optional<ReservationSlot> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<ReservationSlot> findByIdWithLock(long id) {
        return findById(id);
    }

    @Override
    public Optional<ReservationSlot> findByDateAndTimeIdAndThemeId(LocalDate date, long timeId, long themeId) {
        return storage.values().stream()
                .filter(slot -> slot.getDate().equals(date))
                .filter(slot -> slot.getTimeSlot().getId().equals(timeId))
                .filter(slot -> slot.getTheme().getId().equals(themeId))
                .findAny();
    }

    @Override
    public ReservationSlot save(ReservationSlot reservationSlot) {
        long id = sequence++;
        ReservationSlot savedSlot = new ReservationSlot(
                id,
                reservationSlot.getDate(),
                reservationSlot.getTimeSlot(),
                reservationSlot.getTheme()
        );
        storage.put(id, savedSlot);
        return savedSlot;
    }
}

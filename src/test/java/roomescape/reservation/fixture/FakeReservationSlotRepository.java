package roomescape.reservation.fixture;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.reservation.repository.ReservationSlotRepository;

public class FakeReservationSlotRepository implements ReservationSlotRepository {

    private final Map<Long, ReservationSlot> store = new HashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public void saveIfAbsent(ReservationSlot reservationSlot) {
        if (existsByDateTimeAndThemeId(reservationSlot.getDateId(), reservationSlot.getTimeId(),
            reservationSlot.getThemeId())) {
            return;
        }
        Long id = idGenerator.getAndIncrement();
        ReservationSlot saved = reservationSlot.withId(id);
        store.put(id, saved);
    }

    @Override
    public void lockByDateTimeAndThemeId(Long dateId, Long timeId, Long themeId) {

    }

    private boolean existsByDateTimeAndThemeId(Long dateId, Long timeId, Long themeId) {
        return store.values().stream()
            .anyMatch(reservationSlot -> reservationSlot.getDateId().equals(dateId)
                && reservationSlot.getTimeId().equals(timeId) && reservationSlot.getThemeId()
                .equals(themeId));
    }
}

package roomescape.slot.fixture;

import roomescape.slot.domain.ReservationSlot;
import roomescape.slot.repository.ReservationSlotRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeReservationSlotRepository implements ReservationSlotRepository {

    private final List<ReservationSlot> store = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public ReservationSlot save(ReservationSlot slot) {
        Long id = idGenerator.getAndIncrement();
        ReservationSlot saved = ReservationSlot.load(id, slot.getDate(), slot.getTime(), slot.getTheme());
        store.add(saved);
        return saved;
    }

    @Override
    public List<ReservationSlot> findAll() {
        return List.copyOf(store);
    }

    @Override
    public Optional<ReservationSlot> findByDateIdTimeIdThemeId(Long dateId, Long timeId, Long themeId) {
        return store.stream()
                .filter(slot -> slot.getDateId().equals(dateId)
                        && slot.getTimeId().equals(timeId)
                        && slot.getThemeId().equals(themeId))
                .findFirst();
    }

}

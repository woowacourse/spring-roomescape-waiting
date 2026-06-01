package roomescape.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.slot.Slot;
import roomescape.repository.SlotDao;

public class FakeSlotDao extends SlotDao {

    private final List<Slot> store = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public FakeSlotDao() {
        super(null);
    }

    public Slot save(Slot slot) {
        Slot withId = slot.getId() == null ? slot.withId(idGenerator.getAndIncrement()) : slot;
        store.add(withId);
        return withId;
    }

    @Override
    public Optional<Slot> findByDateAndTimeAndTheme(LocalDate date, Long timeId, Long themeId) {
        return store.stream()
                .filter(s -> s.getDate().equals(date)
                        && s.getTime().getId().equals(timeId)
                        && s.getTheme().getId().equals(themeId))
                .findFirst();
    }

    @Override
    public Optional<Slot> findById(Long id) {
        return store.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst();
    }

    @Override
    public Long insert(Slot slot) {
        return save(slot).getId();
    }

    @Override
    public long deleteIfNoWaiting(Long id) {
        return store.removeIf(s -> s.getId().equals(id)) ? 1 : 0;
    }
}

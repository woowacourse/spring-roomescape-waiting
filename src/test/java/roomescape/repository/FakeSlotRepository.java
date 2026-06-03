package roomescape.repository;

import roomescape.domain.Slot;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FakeSlotRepository implements SlotRepository {

    private final Map<Long, Slot> storage = new HashMap<>();
    private long sequence = 1L;

    @Override
    public Slot save(Slot slot) {
        long id = sequence++;
        Slot savedSlot = new Slot(id, slot.getDate(), slot.getTimeSlot(), slot.getTheme());
        storage.put(id, savedSlot);
        return savedSlot;
    }

    @Override
    public Optional<Slot> findByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        return storage.values().stream()
                .filter(slot -> matchCondition(slot, date, timeId, themeId))
                .findAny();
    }

    @Override
    public void deleteById(long id) {
        storage.remove(id);
    }

    private boolean matchCondition(Slot slot, LocalDate date, Long timeId, Long themeId) {
        return slot.getDate().equals(date)
                && slot.getTimeSlot().getId().equals(timeId)
                && slot.getTheme().getId().equals(themeId);
    }
}

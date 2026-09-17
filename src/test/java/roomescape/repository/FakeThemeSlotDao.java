package roomescape.repository;

import roomescape.domain.ThemeSlot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FakeThemeSlotDao implements ThemeSlotRepository {

    private final Map<Long, ThemeSlot> storage = new HashMap<>();
    private final List<Long> findByIdForUpdateHistory = new ArrayList<>();
    private long sequence = 1L;

    @Override
    public ThemeSlot save(ThemeSlot themeSlot) {
        long id = sequence++;
        ThemeSlot saved = ThemeSlot.of(id, themeSlot);
        storage.put(id, saved);
        return saved;
    }

    @Override
    public List<ThemeSlot> findByThemeIdAndDate(long themeId, LocalDate date) {
        return storage.values().stream()
                .filter(ts -> ts.getTheme().getId() == themeId && ts.getDate().equals(date))
                .toList();
    }

    @Override
    public Optional<ThemeSlot> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<ThemeSlot> findByIdForUpdate(long id) {
        findByIdForUpdateHistory.add(id);
        return findById(id);
    }

    @Override
    public List<ThemeSlot> findAllByIdsForUpdateInOrder(Long firstId, Long secondId) {
        List<ThemeSlot> themeSlots = storage.values()
                .stream()
                .filter(themeSlot -> themeSlot.hasSameId(firstId) || themeSlot.hasSameId(secondId))
                .sorted(Comparator.comparing(ThemeSlot::getId))
                .toList();
        themeSlots.forEach(themeSlot -> findByIdForUpdateHistory.add(themeSlot.getId()));
        return themeSlots;
    }

    @Override
    public void updateReserved(ThemeSlot themeSlot) {
        storage.computeIfPresent(themeSlot.getId(), (id, saved) -> ThemeSlot.of(id, themeSlot));
    }

    public List<Long> findByIdForUpdateHistory() {
        return List.copyOf(findByIdForUpdateHistory);
    }

    public void clearFindByIdForUpdateHistory() {
        findByIdForUpdateHistory.clear();
    }
}

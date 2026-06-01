package roomescape.repository;

import roomescape.domain.ThemeSlot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FakeThemeSlotRepository implements ThemeSlotRepository {

    private final Map<Long, ThemeSlot> storage = new HashMap<>();
    private long sequence = 1L;

    @Override
    public ThemeSlot save(ThemeSlot themeSlot) {
        long id = sequence++;
        ThemeSlot saved = ThemeSlot.of(id, themeSlot);
        storage.put(id, saved);
        return saved;
    }

    @Override
    public List<ThemeSlot> saveAll(List<ThemeSlot> themeSlots) {
        List<ThemeSlot> results = new ArrayList<>();
        for (ThemeSlot themeSlot : themeSlots) {
            results.add(save(themeSlot));
        }
        return results;
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
    public boolean isExistBy(long themeId, LocalDate date) {
        return storage.values().stream()
                .anyMatch(ts -> ts.getTheme().getId() == themeId && ts.getDate().equals(date));
    }

    @Override
    public void update(ThemeSlot themeSlot) {
        storage.values().stream()
                .filter(ts -> ts.getTheme().getId().equals(themeSlot.getTheme().getId())
                        && ts.getDate().equals(themeSlot.getDate())
                        && ts.getTime().getId().equals(themeSlot.getTime().getId()))
                .findFirst()
                .ifPresent(ts -> storage.put(ts.getId(), ThemeSlot.of(ts.getId(), themeSlot)));
    }

    @Override
    public void deleteById(long id) {
        storage.remove(id);
    }
}

package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import roomescape.domain.Theme;

public class FakeThemeRepository implements ThemeRepository {

    private final Map<Long, Theme> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1L);

    public void clear() {
        storage.clear();
        sequence.set(1L);
    }

    @Override
    public List<Theme> findAll() {
        return List.copyOf(storage.values());
    }

    @Override
    public Optional<Theme> findById(long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Theme save(Theme theme) {
        long id = sequence.getAndIncrement();
        Theme savedTheme = new Theme(id, theme.getName(), theme.getDescription(), theme.getThumbnailUrl(), theme.getPrice());
        storage.put(id, savedTheme);
        return savedTheme;
    }

    @Override
    public void deleteById(long id) {
        storage.remove(id);
    }

    @Override
    public List<Theme> findPopularThemes(Long topCount, LocalDate fromDate, LocalDate toDate) {
        return List.of();
    }
}

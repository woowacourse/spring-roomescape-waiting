package roomescape.theme.unit.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.theme.entity.Theme;
import roomescape.theme.repository.ThemeRepository;

public class FakeThemeRepository implements ThemeRepository {

    private final Map<Long, Theme> themes = new ConcurrentHashMap<>();
    private final AtomicLong index = new AtomicLong(1);

    @Override
    public Theme save(Theme theme) {
        Long id = index.getAndIncrement();
        Theme savedTheme = new Theme(
                id,
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail()
        );
        themes.put(id, savedTheme);
        return savedTheme;
    }

    @Override
    public List<Theme> findAll() {
        return new ArrayList<>(themes.values());
    }

    @Override
    public List<Theme> findPopularDescendingUpTo(LocalDate startDate, LocalDate endDate, int limit) {
        return themes.values().stream()
                .limit(limit)
                .toList();
    }

    @Override
    public Optional<Theme> findById(Long id) {
        return Optional.ofNullable(themes.get(id));
    }

    @Override
    public Optional<Theme> findByName(String name) {
        return themes.values().stream()
                .filter(theme -> theme.getName().equals(name))
                .findFirst();
    }

    @Override
    public boolean deleteById(Long id) {
        return themes.remove(id) != null;
    }
}

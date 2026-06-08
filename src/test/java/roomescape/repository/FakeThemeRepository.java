package roomescape.repository;

import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FakeThemeRepository implements ThemeRepository {

    private final Map<Long, Theme> storage = new HashMap<>();
    private long sequence = 1L;

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
        long id = sequence++;
        Theme savedTheme = new Theme(id, theme.getName(), theme.getDescription(), theme.getThumbnailUrl());
        storage.put(id, savedTheme);
        return savedTheme;
    }

    @Override
    public void deleteById(long id) {
        storage.remove(id);
    }

    @Override
    public List<Theme> findPopularThemes(long limit, LocalDate startDate, LocalDate endDate) {
        return List.of();
    }
}

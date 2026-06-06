package roomescape.support;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.theme.Theme;
import roomescape.repository.theme.ThemeRepository;

public class FakeThemeRepository implements ThemeRepository {

    private final Map<Long, Theme> themes = new LinkedHashMap<>();
    private long sequence = 1L;

    @Override
    public List<Theme> findAll() {
        return new ArrayList<>(themes.values());
    }

    @Override
    public Optional<Theme> findById(final long id) {
        return Optional.ofNullable(themes.get(id));
    }

    @Override
    public int deleteById(final long id) {
        if (themes.remove(id) == null) {
            return 0;
        }

        return 1;
    }

    @Override
    public Theme save(final Theme theme) {
        Theme saved = theme;
        if (saved.getId() == null) {
            saved = Theme.of(sequence++, theme.getName(), theme.getDescription(), theme.getThumbnailUrl());
        }

        themes.put(saved.getId(), saved);
        return saved;
    }

    @Override
    public boolean existsByName(final String name) {
        return themes.values()
                .stream()
                .anyMatch(theme -> theme.getName().equals(name));
    }

    @Override
    public List<Theme> findPopularThemes(final int period, final int limit) {
        return findAll().stream()
                .limit(limit)
                .toList();
    }
}

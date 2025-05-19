package roomescape.fake;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepositoryInterface;

public class FakeThemeRepository implements ThemeRepositoryInterface {

    private final Map<Long, Theme> themes = new HashMap<>();

    private long sequence = 0;

    @Override
    public boolean existsByName(final String name) {
        return themes.values().stream()
                .anyMatch(theme -> theme.getName().equals(name));
    }

    @Override
    public Optional<Theme> findById(final Long id) {
        return Optional.ofNullable(themes.get(id));
    }

    @Override
    public Theme save(final Theme theme) {
        sequence++;
        Theme newTheme = new Theme(sequence, theme.getName(), theme.getDescription(), theme.getThumbnail());
        themes.put(sequence, newTheme);
        return newTheme;
    }

    @Override
    public List<Theme> findAll() {
        return List.copyOf(themes.values());
    }

    @Override
    public void deleteById(final Long id) {
        themes.remove(id);
    }
}

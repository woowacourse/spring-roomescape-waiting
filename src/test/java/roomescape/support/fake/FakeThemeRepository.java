package roomescape.support.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeRepository;

public class FakeThemeRepository implements ThemeRepository {

    private final List<Theme> themes = new ArrayList<>();
    private Long index = 1L;

    @Override
    public boolean existsByThemeName(final ThemeName themeName) {
        return themes.stream()
                .anyMatch(theme -> theme.getNameOfTheme().equals(themeName.name()));
    }

    @Override
    public Theme save(final Theme theme) {
        final Theme savedTheme = new Theme(index++, theme.getNameOfTheme(), theme.getDescriptionOfTheme(),
                theme.getThumbnailOfTheme());
        themes.add(savedTheme);
        return savedTheme;
    }

    @Override
    public void deleteById(final long id) {
        final Theme theme = findById(id).orElseThrow();
        themes.remove(theme);
    }

    @Override
    public List<Theme> findAll() {
        return themes;
    }

    @Override
    public Optional<Theme> findById(final long id) {
        final Theme result = themes.stream()
                .filter(theme -> theme.id() == id)
                .findAny()
                .orElse(null);
        return Optional.ofNullable(result);
    }

    @Override
    public List<Theme> findPopularThemes(final LocalDate from, final LocalDate to, final int count) {
        return null;
    }
}

package roomescape.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.repository.ThemeDao;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeName;

public class FakeThemeDao implements ThemeDao {

    List<Theme> themes = new ArrayList<>();
    Long index = 1L;

    @Override
    public boolean isExists(final ThemeName name) {
        return themes.stream()
                .anyMatch(theme -> theme.getName().equals(name.getName()));
    }

    @Override
    public Theme save(final Theme theme) {
        Theme savedTheme = new Theme(index++, theme.getName(), theme.getDescription(), theme.getThumbnail());
        themes.add(savedTheme);
        return savedTheme;
    }

    @Override
    public List<Theme> findAll() {
        return themes;
    }

    @Override
    public Optional<Theme> findById(final long id) {
        Theme result = themes.stream()
                .filter(theme -> theme.getId() == id)
                .findAny()
                .orElse(null);
        return Optional.ofNullable(result);
    }

    @Override
    public void deleteById(long id) {
        Theme theme = findById(id).orElseThrow();
        themes.remove(theme);
    }

    @Deprecated
    @Override
    public List<Theme> findPopularThemes(LocalDate from, LocalDate to, int count) {
        return null;
    }
}

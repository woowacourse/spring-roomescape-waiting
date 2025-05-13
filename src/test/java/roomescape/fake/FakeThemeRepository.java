package roomescape.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.ThemeName;
import roomescape.reservation.repository.ThemeRepository;

public class FakeThemeRepository implements ThemeRepository {

    List<Theme> themes = new ArrayList<>();
    Long index = 1L;

    public boolean existsByName(final ThemeName name) {
        return themes.stream()
                .anyMatch(theme -> theme.getName().equals(name.getName()));
    }

    public Theme save(final Theme theme) {
        Theme savedTheme = new Theme(index++, theme.getName(), theme.getDescription(), theme.getThumbnail());
        themes.add(savedTheme);
        return savedTheme;
    }

    public List<Theme> findAll() {
        return themes;
    }

    public Optional<Theme> findById(final long id) {
        Theme result = themes.stream()
                .filter(theme -> theme.getId() == id)
                .findAny()
                .orElse(null);
        return Optional.ofNullable(result);
    }

    public void deleteById(long id) {
        Theme theme = findById(id).orElseThrow();
        themes.remove(theme);
    }

    public List<Theme> findPopularThemes(LocalDate from, LocalDate to, int count) {
        return null;
    }
}

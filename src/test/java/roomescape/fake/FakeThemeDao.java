package roomescape.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.repository.ThemeRepository;

public class FakeThemeDao implements ThemeRepository {

    List<Theme> themes = new ArrayList<>();
    Long index = 1L;

    @Override
    public boolean existsByName(final ThemeName name) {
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
        return themes.stream()
                .filter(theme -> theme.getId() == id)
                .findAny();
    }

    /**
     * TODO
     * JOIN을 구현할 방법 찾기
     */
    @Override
    public List<Theme> findPopularThemes(LocalDate from, LocalDate to, int count) {
        return null;
    }

    @Override
    public void deleteById(long id) {
        Theme theme = findById(id).orElseThrow();
        themes.remove(theme);
    }
}

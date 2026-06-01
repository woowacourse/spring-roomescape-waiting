package roomescape.service.fake;

import roomescape.domain.Theme;
import roomescape.repository.ThemeRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FakeThemeRepository implements ThemeRepository {

    private final List<Theme> themes = new ArrayList<>();

    public void add(Theme theme) {
        themes.add(theme);
    }

    @Override
    public Optional<Theme> findById(long themeId) {
        return themes.stream()
                .filter(t -> t.getId() == themeId)
                .findFirst();
    }

    @Override
    public List<Theme> findAll() {
        return List.copyOf(themes);
    }

    @Override
    public List<Long> findReservedTimeIds(long themeId, LocalDate date) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Theme> findPopularThemes(LocalDate startDate, LocalDate endDate, int limit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Theme save(Theme theme) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteById(long id) {
        throw new UnsupportedOperationException();
    }
}

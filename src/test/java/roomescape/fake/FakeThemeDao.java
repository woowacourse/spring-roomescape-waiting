package roomescape.fake;

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
                .anyMatch(theme -> theme.getName().getValue().equals(name.getValue()));
    }

    @Override
    public Theme save(final Theme theme) {
        Theme savedTheme = new Theme(index++, theme.getName().getValue(), theme.getDescription().getValue(),
                theme.getThumbnail().getValue());
        themes.add(savedTheme);
        return savedTheme;
    }

    @Override
    public <S extends Theme> Iterable<S> saveAll(final Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<Theme> findById(final Long id) {
        return themes.stream()
                .filter(theme -> theme.getId() == id)
                .findAny();
    }

    @Override
    public boolean existsById(final Long aLong) {
        return false;
    }

    @Override
    public List<Theme> findAll() {
        return themes;
    }

    @Override
    public Iterable<Theme> findAllById(final Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(final Long id) {
        Theme theme = findById(id).orElseThrow();
        themes.remove(theme);
    }

    @Override
    public void delete(final Theme entity) {

    }

    @Override
    public void deleteAllById(final Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(final Iterable<? extends Theme> entities) {

    }

    @Override
    public void deleteAll() {

    }
}

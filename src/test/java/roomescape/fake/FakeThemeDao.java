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
    public Theme save(final Theme theme) {
        Theme savedTheme = new Theme(index++, theme.getName().getValue(), theme.getDescription().getValue(),
                theme.getThumbnail().getValue());
        themes.add(savedTheme);
        return savedTheme;
    }

    @Override
    public List<Theme> findAll() {
        return themes;
    }

    @Override
    public Optional<Theme> findById(final Long id) {
        return themes.stream()
                .filter(theme -> theme.getId() == id)
                .findAny();
    }

    @Override
    public void deleteById(final Long id) {
        Theme theme = findById(id).orElseThrow();
        themes.remove(theme);
    }

    @Override
    public boolean existsByName(final ThemeName name) {
        return themes.stream()
                .anyMatch(theme -> theme.getName().getValue().equals(name.getValue()));
    }

    @Override
    public <S extends Theme> Iterable<S> saveAll(final Iterable<S> entities) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public boolean existsById(final Long aLong) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public Iterable<Theme> findAllById(final Iterable<Long> longs) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public long count() {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public void delete(final Theme entity) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public void deleteAllById(final Iterable<? extends Long> longs) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public void deleteAll(final Iterable<? extends Theme> entities) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public void deleteAll() {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }
}

package roomescape.fake;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.theme.domain.Theme;
import roomescape.theme.repository.ThemeRepository;

public class FakeThemeRepository implements ThemeRepository {

    private final Map<Long, Theme> themes = new HashMap<>();

    private long sequence = 0;

    @Override
    public boolean existsByName(final String name) {
        return themes.values().stream()
                .anyMatch(theme -> theme.getName().equals(name));
    }

    @Override
    public <S extends Theme> S save(final S entity) {
        sequence++;
        Theme theme = new Theme(sequence, entity.getName(), entity.getDescription(), entity.getThumbnail());
        themes.put(sequence, theme);
        return (S) theme;
    }

    @Override
    public Optional<Theme> findById(final Long id) {
        return Optional.ofNullable(themes.get(id));
    }

    @Override
    public List<Theme> findAll() {
        return List.copyOf(themes.values());
    }

    @Override
    public void deleteById(final Long id) {
        themes.remove(id);
    }

    @Override
    public <S extends Theme> Iterable<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public Iterable<Theme> findAllById(Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void delete(Theme entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(Iterable<? extends Theme> entities) {

    }

    @Override
    public void deleteAll() {

    }
}

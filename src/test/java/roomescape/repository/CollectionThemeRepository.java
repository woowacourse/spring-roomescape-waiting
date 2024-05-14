package roomescape.repository;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.Theme;
import roomescape.domain.Themes;

public class CollectionThemeRepository implements ThemeRepository {

    private final List<Theme> themes;
    private final AtomicLong index;

    public CollectionThemeRepository() {
        themes = new ArrayList<>();
        index = new AtomicLong(0);
    }

    @Override
    public Themes findAll() {
        return new Themes(themes);
    }

    @Override
    public Optional<Theme> findById(long id) {
        return themes.stream()
                .filter(theme -> theme.isIdOf(id))
                .findFirst();
    }

    @Override
    public Theme save(Theme theme) {
        Theme saved = new Theme(index.incrementAndGet(), theme);
        themes.add(saved);
        return saved;
    }

    @Override
    public void delete(long id) {
        themes.removeIf(theme -> theme.isIdOf(id));
    }
}

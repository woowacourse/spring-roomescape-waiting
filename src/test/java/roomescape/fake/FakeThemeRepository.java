package roomescape.fake;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;

public class FakeThemeRepository implements ThemeRepository {

    private final List<Theme> store = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public void save(Theme theme) {
        store.add(theme);
    }

    @Override
    public Optional<Theme> findThemeById(long id) {
        return store.stream()
                .filter(theme -> theme.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Theme> findAllTheme() {
        return List.copyOf(store);
    }

    @Override
    public List<Theme> findAllByTopTheme() {
        return List.copyOf(store);
    }

    @Override
    public Long insert(Theme theme) {
        long id = idGenerator.getAndIncrement();
        store.add(new Theme(id, theme.getName(), theme.getDescription(), theme.getUrl()));
        return id;
    }

    @Override
    public void delete(Long id) {
        store.removeIf(theme -> theme.getId().equals(id));
    }
}

package roomescape.theme.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.test.util.ReflectionTestUtils;
import roomescape.theme.domain.Theme;

public class FakeThemeRepository implements ThemeRepository {

    private final Map<Long, Theme> themes = new ConcurrentHashMap<>();
    private final AtomicLong id = new AtomicLong(0);

    public FakeThemeRepository() {
        long increasedId = id.incrementAndGet();
        themes.put(increasedId, makeTheme(Theme.of("pollaBang", "폴라 방탈출", "thumbnail"), increasedId));
    }

    @Override
    public Theme save(Theme theme) {
        long increasedId = id.incrementAndGet();
        themes.put(increasedId, makeTheme(theme, increasedId));
        return theme;
    }

    @Override
    public List<Theme> findAll() {
        return themes.values().stream()
                .toList();
    }

    @Override
    public Optional<Theme> findById(long id) {
        return Optional.ofNullable(themes.get(id));
    }

    @Override
    public void deleteById(long themeId) {
        themes.remove(themeId);
    }

    private Theme makeTheme(Theme theme, long id) {
        ReflectionTestUtils.setField(theme, "id", id);
        return theme;
    }
}

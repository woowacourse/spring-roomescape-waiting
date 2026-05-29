package roomescape.fake;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.domain.theme.Theme;
import roomescape.repository.ThemeQueryingDao;

public class FakeThemeQueryingDao extends ThemeQueryingDao {

    private final Map<Long, Theme> store = new HashMap<>();

    public FakeThemeQueryingDao() {
        super(null);
    }

    public void save(Theme theme) {
        store.put(theme.getId(), theme);
    }

    @Override
    public Optional<Theme> findThemeById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Theme> findAllTheme() {
        return List.copyOf(store.values());
    }
}

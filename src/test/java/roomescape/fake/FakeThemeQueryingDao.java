package roomescape.fake;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import roomescape.domain.theme.Theme;
import roomescape.repository.ThemeQueryingDao;

public class FakeThemeQueryingDao extends ThemeQueryingDao {

    private final List<Theme> store = new ArrayList<>();

    public FakeThemeQueryingDao() {
        super(null);
    }

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
}

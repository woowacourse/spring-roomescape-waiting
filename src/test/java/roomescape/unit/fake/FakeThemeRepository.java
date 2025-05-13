package roomescape.unit.fake;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.theme.DateRange;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeDescription;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThemeThumbnail;
import roomescape.repository.ThemeRepository;

public class FakeThemeRepository implements ThemeRepository {
    private final List<Theme> values = new ArrayList();
    private final AtomicLong increment = new AtomicLong(1);

    @Override
    public Theme save(final ThemeName name, final ThemeDescription description, final ThemeThumbnail thumbnail) {
        Theme theme = new Theme(increment.getAndIncrement(), name, description, thumbnail);
        values.add(theme);
        return theme;
    }

    @Override
    public void deleteById(final Long id) {
        values.removeIf(theme -> theme.getId().equals(id));
    }

    @Override
    public List<Theme> findAll() {
        return Collections.unmodifiableList(values);
    }

    @Override
    public Optional<Theme> findById(final Long id) {
        return values.stream().filter(theme -> theme.getId().equals(id)).findFirst();
    }

    @Override
    public List<Theme> findPopularThemeDuringAWeek(final long limit, final DateRange dateRange) {
        return findAll();
    }
}

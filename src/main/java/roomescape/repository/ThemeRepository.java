package roomescape.repository;

import java.util.List;
import java.util.Optional;
import roomescape.domain.theme.DateRange;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeDescription;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThemeThumbnail;

public interface ThemeRepository {

    Theme save(
            final ThemeName name,
            final ThemeDescription description,
            final ThemeThumbnail thumbnail
    );

    void deleteById(final Long id);

    List<Theme> findAll();

    Optional<Theme> findById(final Long id);

    List<Theme> findPopularThemeDuringAWeek(final long limit, final DateRange dateRange);
}

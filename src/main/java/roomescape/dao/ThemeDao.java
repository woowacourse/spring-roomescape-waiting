package roomescape.dao;

import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemePopularFilter;

import java.util.List;
import java.util.Optional;

public interface ThemeDao {

    Theme save(final Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(final Long id);

    void deleteById(final Long id);

    List<Theme> findPopularThemesBy(final ThemePopularFilter themePopularFilter);
}

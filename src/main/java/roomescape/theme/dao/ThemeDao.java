package roomescape.theme.dao;

import java.util.List;
import java.util.Optional;
import roomescape.theme.domain.Theme;

public interface ThemeDao {

    Theme save(Theme theme);

    Optional<Theme> findById(long themeId);

    List<Theme> findAllThemes();

    void deleteById(long themeId);
}

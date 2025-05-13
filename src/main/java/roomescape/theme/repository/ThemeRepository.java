package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeName;

public interface ThemeRepository  {

    Theme save(Theme theme);

    List<Theme> findAll();

    Optional<Theme> findById(long id);

    List<Theme> findPopularThemes(LocalDate from, LocalDate to, int count);

    void deleteById(long id);

    boolean existsByName(ThemeName name);
}

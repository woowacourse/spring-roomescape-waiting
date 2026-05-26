package roomescape.theme.infrastructure;

import roomescape.theme.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ThemeRepository {
    Theme save(Theme domain);

    void deleteById(long id);

    List<Theme> findScheduledThemesByDate(LocalDate date);

    List<Theme> findPopularThemeByCurrentDate(LocalDate currentDate);

    Optional<Theme> findById(long id);

    List<Theme> findAll();

    boolean existsAlreadyTheme(String name);
}

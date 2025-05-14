package roomescape.dao;

import java.time.LocalDate;
import java.util.List;
import roomescape.entity.Theme;

public interface ThemeDao {

    List<Theme> findAllThemes();

    Theme findThemeById(Long id);

    List<Theme> findTopReservedThemesInPeriodWithLimit(LocalDate startDate, LocalDate endDate, int limitCount);

    boolean existThemeByName(String name);

    Theme addTheme(Theme theme);

    void removeThemeById(Long id);
}

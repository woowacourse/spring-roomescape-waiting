package roomescape.repository;

import java.time.LocalDate;
import java.util.List;

import roomescape.model.Theme;

public interface ThemeDao {

    List<Theme> findAllThemes();

    Theme addTheme(Theme theme);

    void deleteTheme(long id);

    Theme findThemeById(long id);

    List<Theme> findThemeRankingByDate(LocalDate before, LocalDate after, int limit);
}

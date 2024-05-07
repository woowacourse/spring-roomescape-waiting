package roomescape.theme.dao;

import java.time.LocalDate;
import java.util.List;
import roomescape.theme.domain.Theme;

public interface ThemeDao {

    Theme save(Theme theme);

    List<Theme> findAll();

    List<Theme> findByDateOrderByCount(LocalDate startDate, LocalDate endDate);

    void deleteById(long themeId);

    Theme findById(long themeId);

}

package roomescape.theme.dao;

import java.time.LocalDate;
import java.util.List;
import roomescape.common.Dao;
import roomescape.theme.domain.Theme;

public interface ThemeDao extends Dao<Theme> {
    List<Theme> findRankedByPeriod(LocalDate startDate, LocalDate endDate, int count);

    Boolean existsByName(String name);

    Boolean existsByReservationThemeId(Long themeId);
}

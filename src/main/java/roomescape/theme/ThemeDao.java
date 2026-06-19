package roomescape.theme;

import java.time.LocalDate;
import java.util.List;
import roomescape.common.CommonDao;
import roomescape.common.vo.Name;
import roomescape.theme.web.AvailableTimeResponseDto;

public interface ThemeDao extends CommonDao<Theme> {
    boolean existsByName(Name name);

    List<AvailableTimeResponseDto> findAvailableTimesById(Long themeId, LocalDate localDate);

    List<ThemeReservationCount> findReservationCounts(LocalDate from, LocalDate to);
}

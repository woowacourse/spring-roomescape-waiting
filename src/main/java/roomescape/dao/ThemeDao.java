package roomescape.dao;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeReservationCount;
import roomescape.common.vo.Name;
import roomescape.dto.response.AvailableTimeResponseDto;

public interface ThemeDao extends CommonDao<Theme> {
    boolean existsByName(Name name);

    List<AvailableTimeResponseDto> findAvailableTimesById(Long themeId, LocalDate localDate);

    List<ThemeReservationCount> findReservationCounts(LocalDate from, LocalDate to);
}

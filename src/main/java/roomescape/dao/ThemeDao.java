package roomescape.dao;

import java.time.LocalDate;
import java.util.List;
import roomescape.domain.Theme;
import roomescape.domain.vo.Name;
import roomescape.dto.response.AvailableTimeResponseDto;

public interface ThemeDao extends CommonDao<Theme> {
    boolean existsByName(Name name);

    List<AvailableTimeResponseDto> findAvailableTimesById(Long themeId, LocalDate localDate);

    List<Theme> findPopulars(LocalDate from, LocalDate to, int limit);
}

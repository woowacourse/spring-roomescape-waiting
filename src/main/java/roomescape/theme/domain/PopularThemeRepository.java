package roomescape.theme.domain;

import java.time.LocalDate;
import java.util.List;

public interface PopularThemeRepository {

    List<Theme> findTopNByPeriod(LocalDate startAt, LocalDate endAt, ThemeSortType sortType, Long limit);
}

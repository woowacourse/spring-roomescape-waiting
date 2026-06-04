package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.reservation.query.dto.PopularThemeQueryResult;

public interface ReservationQueryDao {

    List<PopularThemeQueryResult> queryPopularThemes(LocalDate from, LocalDate to, int limit);
}

package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import roomescape.reservation.query.dto.PopularThemeQueryResult;
import roomescape.reservation.query.dto.ReservationWithStatusResult;

public interface ReservationQueryDao {

    List<ReservationWithStatusResult> queryAllByNameWithStatus(String name);

    List<PopularThemeQueryResult> queryPopularThemes(LocalDate from, LocalDate to, int limit);
}

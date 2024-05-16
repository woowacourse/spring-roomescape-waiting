package roomescape.reservation.repository;

import roomescape.reservation.model.ReservationDate;
import roomescape.reservation.model.Theme;

import java.util.List;

public interface CustomThemeRepository {

    List<Theme> findPopularThemes(final ReservationDate startAt, final ReservationDate endAt, final int maximumThemeCount);
}

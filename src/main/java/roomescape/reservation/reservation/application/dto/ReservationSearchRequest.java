package roomescape.reservation.reservation.application.dto;

import roomescape.reservation.reservation.domain.ReservationDate;
import roomescape.theme.domain.ThemeId;
import roomescape.user.domain.UserId;

public record ReservationSearchRequest(
        ThemeId themeId,
        UserId userId,
        ReservationDate dateFrom,
        ReservationDate dateTo
) {

}

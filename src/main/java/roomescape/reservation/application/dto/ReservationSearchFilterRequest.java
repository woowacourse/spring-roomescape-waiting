package roomescape.reservation.application.dto;

import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.domain.ThemeId;
import roomescape.user.domain.UserId;

public record ReservationSearchFilterRequest(
        ThemeId themeId,
        UserId userId,
        ReservationDate dateFrom,
        ReservationDate dateTo
) {

}

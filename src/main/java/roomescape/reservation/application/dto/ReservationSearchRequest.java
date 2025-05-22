package roomescape.reservation.application.dto;

import roomescape.reservation.domain.ReservationDate;
import roomescape.user.domain.UserId;

public record ReservationSearchRequest(
        Long themeId,
        UserId userId,
        ReservationDate dateFrom,
        ReservationDate dateTo
) {

}

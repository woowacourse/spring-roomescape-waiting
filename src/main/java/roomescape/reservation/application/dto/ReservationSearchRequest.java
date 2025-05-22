package roomescape.reservation.application.dto;

import roomescape.reservation.domain.ReservationDate;

public record ReservationSearchRequest(
        Long themeId,
        Long userId,
        ReservationDate dateFrom,
        ReservationDate dateTo
) {

}

package roomescape.service.reservation.dto;

import roomescape.domain.reservation.ReservationDate;

public record ReservationFindRequest(
        Long memberId,
        Long themeId,
        ReservationDate dateFrom,
        ReservationDate dateTo
) {
}

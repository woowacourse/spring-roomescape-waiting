package roomescape.service.dto;

import roomescape.domain.Reservation;

public record ReservationInfoResult(
        Reservation reservation,
        int order
) {
}

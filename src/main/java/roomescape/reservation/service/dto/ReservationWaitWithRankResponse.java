package roomescape.reservation.service.dto;

import roomescape.reservation.domain.ReservationWait;

public record ReservationWaitWithRankResponse(ReservationWait reservationWait, Long rank) {
}

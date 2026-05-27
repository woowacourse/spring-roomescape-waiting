package roomescape.dao.dto;

import roomescape.domain.reservation.time.ReservationTime;

public record TimeQueryResult(
        ReservationTime reservationTime,
        boolean isReserved
) {
}

package roomescape.dao.dto;

import roomescape.domain.slot.time.ReservationTime;

public record TimeQueryResult(
        ReservationTime reservationTime,
        boolean isReserved
) {
}

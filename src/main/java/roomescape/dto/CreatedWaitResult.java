package roomescape.dto;

import roomescape.domain.ReservationWait;

public record CreatedWaitResult(ReservationWait reservationWait, Long order) {
}

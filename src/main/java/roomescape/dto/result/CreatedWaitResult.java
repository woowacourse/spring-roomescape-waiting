package roomescape.dto.result;

import roomescape.domain.ReservationWait;

public record CreatedWaitResult(ReservationWait reservationWait, Long order) {
}

package roomescape.dto.reservationWaiting;

import roomescape.domain.reservationwaiting.ReservationWaiting;

public record ReservationWaitingSequence(ReservationWaiting reservationWaiting, Long sequence) {
}

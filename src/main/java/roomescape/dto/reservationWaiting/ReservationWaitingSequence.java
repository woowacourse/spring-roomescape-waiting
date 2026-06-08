package roomescape.dto.reservationWaiting;

import roomescape.domain.reservatinWaiting.ReservationWaiting;

public record ReservationWaitingSequence(ReservationWaiting reservationWaiting, Long sequence) {
}

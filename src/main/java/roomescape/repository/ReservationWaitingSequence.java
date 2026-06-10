package roomescape.repository;

import roomescape.domain.reservationwaiting.ReservationWaiting;

public record ReservationWaitingSequence(ReservationWaiting reservationWaiting, Long sequence) {
}

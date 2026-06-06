package roomescape.reservationwaiting.repository;

import roomescape.reservationwaiting.domain.ReservationWaiting;

public record WaitingWithTurn(ReservationWaiting waiting, Long turn) {
}
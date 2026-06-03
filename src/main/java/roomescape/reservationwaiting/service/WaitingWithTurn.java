package roomescape.reservationwaiting.service;

import roomescape.reservationwaiting.domain.ReservationWaiting;

public record WaitingWithTurn(ReservationWaiting waiting, Long turn) {
}

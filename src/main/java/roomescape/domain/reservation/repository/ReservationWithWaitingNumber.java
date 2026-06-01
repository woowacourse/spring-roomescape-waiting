package roomescape.domain.reservation.repository;

import roomescape.domain.reservation.entity.Reservation;

public record ReservationWithWaitingNumber(Reservation reservation, Integer waitingNumber) {

}

package roomescape.domain.reservation.dto;

import roomescape.domain.reservation.Reservation;

public record ReservationWithWaitingNumber(
    Reservation reservation,
    Long waitingNumber
) {

}

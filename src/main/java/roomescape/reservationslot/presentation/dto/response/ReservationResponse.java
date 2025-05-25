package roomescape.reservationslot.presentation.dto.response;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

public record ReservationResponse(
        Long reservationId,
        Long waitingId,
        ReservationStatus reservationStatus
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(reservation.getReservation().getId(), reservation.getId(),
                reservation.getWaitingStatus());
    }
}

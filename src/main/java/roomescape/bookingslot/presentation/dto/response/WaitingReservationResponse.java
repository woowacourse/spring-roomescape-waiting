package roomescape.bookingslot.presentation.dto.response;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

public record WaitingReservationResponse(
        Long reservationId,
        Long waitingId,
        ReservationStatus reservationStatus
) {
    public static WaitingReservationResponse from(Reservation reservation) {
        return new WaitingReservationResponse(reservation.getReservation().getId(), reservation.getId(),
                reservation.getWaitingStatus());
    }
}

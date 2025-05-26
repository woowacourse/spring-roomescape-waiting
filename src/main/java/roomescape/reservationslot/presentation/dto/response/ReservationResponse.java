package roomescape.reservationslot.presentation.dto.response;

import roomescape.reservation.domain.Reservation;

public record ReservationResponse(
        Long reservationId,
        Long waitingId
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(reservation.getReservationSlot().getId(), reservation.getId());
    }
}

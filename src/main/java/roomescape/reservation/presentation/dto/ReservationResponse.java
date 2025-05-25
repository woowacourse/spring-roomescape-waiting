package roomescape.reservation.presentation.dto;

import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservation.domain.Reservation;

public record ReservationResponse(Long waitingId, String name, String themeName, String date, String startAt) {

    public static ReservationResponse from(final Reservation reservation) {
        ReservationSlot reservationSlot = reservation.getReservation();
        return new ReservationResponse(
                reservation.getId(),
                reservation.getMember().getName(),
                reservationSlot.getTheme().getName(),
                reservationSlot.getDate().toString(),
                reservationSlot.getTime().getStartAt().toString()
        );
    }
}

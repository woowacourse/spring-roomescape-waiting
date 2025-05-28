package roomescape.reservation.presentation.dto.response;

import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservation.domain.Reservation;

public record WaitingWebResponse(Long reservationId, String name, String themeName, String date, String startAt) {

    public static WaitingWebResponse from(final Reservation reservation) {
        ReservationSlot reservationSlot = reservation.getReservationSlot();
        return new WaitingWebResponse(
                reservation.getId(),
                reservation.getMember().getName(),
                reservationSlot.getTheme().getName(),
                reservationSlot.getDate().toString(),
                reservationSlot.getTime().getStartAt().toString()
        );
    }
}

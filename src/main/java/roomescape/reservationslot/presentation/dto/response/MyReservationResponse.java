package roomescape.reservationslot.presentation.dto.response;

import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservation.domain.Reservation;

public record MyReservationResponse(Long reservationId,
                                    String theme,
                                    String date,
                                    String time,
                                    String status) {

    public static MyReservationResponse from(final Reservation reservation) {
        ReservationSlot reservationSlot = reservation.getReservation();
        String waitingMessage = String.format(
                reservation.getWaitingStatus().getTitle(), reservationSlot.findRank(reservation));
        return new MyReservationResponse(reservationSlot.getId(), reservationSlot.getTheme().getName(),
                reservationSlot.getDate().toString(),
                reservationSlot.getTime().getStartAt().toString(), waitingMessage);
    }
}

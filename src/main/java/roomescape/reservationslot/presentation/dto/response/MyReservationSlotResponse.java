package roomescape.reservationslot.presentation.dto.response;

import roomescape.reservationslot.domain.ReservationSlot;
import roomescape.reservation.domain.Reservation;

public record MyReservationSlotResponse(Long reservationId,
                                        String theme,
                                        String date,
                                        String time,
                                        String status) {

    public static MyReservationSlotResponse from(final Reservation reservation) {
        ReservationSlot reservationSlot = reservation.getReservation();
        String waitingMessage = String.format(
                reservation.getWaitingStatus().getTitle(), reservationSlot.findRank(reservation));
        return new MyReservationSlotResponse(reservationSlot.getId(), reservationSlot.getTheme().getName(),
                reservationSlot.getDate().toString(),
                reservationSlot.getTime().getStartAt().toString(), waitingMessage);
    }
}

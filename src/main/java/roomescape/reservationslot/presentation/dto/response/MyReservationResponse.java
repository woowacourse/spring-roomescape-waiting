package roomescape.reservationslot.presentation.dto.response;

import roomescape.reservation.domain.Reservation;
import roomescape.reservationslot.domain.ReservationSlot;

public record MyReservationResponse(Long reservationSlotId,
                                    String theme,
                                    String date,
                                    String time,
                                    boolean isReserved,
                                    long waitingRank) {

    public static MyReservationResponse from(final Reservation reservation) {
        ReservationSlot reservationSlot = reservation.getReservationSlot();
        return new MyReservationResponse(reservationSlot.getId(), reservationSlot.getTheme().getName(),
                reservationSlot.getDate().toString(),
                reservationSlot.getTime().getStartAt().toString(), reservation.isReserved(),
                reservationSlot.findRank(reservation));
    }
}

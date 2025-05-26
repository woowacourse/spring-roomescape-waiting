package roomescape.reservationslot.presentation.dto.response;

import roomescape.reservation.domain.Reservation;
import roomescape.reservationslot.domain.ReservationSlot;

public record MyReservationSlotResponse(Long reservationId,
                                        String theme,
                                        String date,
                                        String time,
                                        boolean isReserved,
                                        long waitingRank) {

    public static MyReservationSlotResponse from(final Reservation reservation) {
        ReservationSlot reservationSlot = reservation.getReservationSlot();
        return new MyReservationSlotResponse(reservationSlot.getId(), reservationSlot.getTheme().getName(),
                reservationSlot.getDate().toString(),
                reservationSlot.getTime().getStartAt().toString(), reservation.isReserved(),
                reservationSlot.findRank(reservation));
    }
}

package roomescape.bookingslot.presentation.dto.response;

import roomescape.bookingslot.domain.BookingSlot;
import roomescape.reservation.domain.Reservation;

public record MyReservationResponse(Long reservationId,
                                    String theme,
                                    String date,
                                    String time,
                                    String status) {

    public static MyReservationResponse from(final Reservation reservation) {
        BookingSlot bookingSlot = reservation.getReservation();
        String waitingMessage = String.format(
                reservation.getWaitingStatus().getTitle(), bookingSlot.findRank(reservation));
        return new MyReservationResponse(bookingSlot.getId(), bookingSlot.getTheme().getName(),
                bookingSlot.getDate().toString(),
                bookingSlot.getTime().getStartAt().toString(), waitingMessage);
    }
}

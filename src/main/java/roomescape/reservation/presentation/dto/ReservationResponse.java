package roomescape.reservation.presentation.dto;

import roomescape.bookingslot.domain.BookingSlot;
import roomescape.reservation.domain.Reservation;

public record ReservationResponse(Long waitingId, String name, String themeName, String date, String startAt) {

    public static ReservationResponse from(final Reservation reservation) {
        BookingSlot bookingSlot = reservation.getReservation();
        return new ReservationResponse(
                reservation.getId(),
                reservation.getMember().getName(),
                bookingSlot.getTheme().getName(),
                bookingSlot.getDate().toString(),
                bookingSlot.getTime().getStartAt().toString()
        );
    }
}

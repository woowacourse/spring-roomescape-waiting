package roomescape.reservation.service;

import roomescape.reservation.domain.Reservation;
import roomescape.reservationwaiting.domain.ReservationWaiting;

public record BookingResult(Reservation reservation, ReservationWaiting waiting) {

    public static BookingResult reserved(Reservation reservation) {
        return new BookingResult(reservation, null);
    }

    public static BookingResult waiting(ReservationWaiting waiting) {
        return new BookingResult(null, waiting);
    }

    public boolean isWaiting() {
        return waiting != null;
    }
}

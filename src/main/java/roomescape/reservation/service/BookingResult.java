package roomescape.reservation.service;

import roomescape.reservation.domain.Reservation;
import roomescape.reservationwaiting.domain.ReservationWaiting;

public record BookingResult(Reservation reservation, ReservationWaiting waiting, String orderId, Long amount) {

    public static BookingResult pendingPayment(Reservation reservation, String orderId, Long amount) {
        return new BookingResult(reservation, null, orderId, amount);
    }

    public static BookingResult waiting(ReservationWaiting waiting) {
        return new BookingResult(null, waiting, null, null);
    }

    public boolean isWaiting() {
        return waiting != null;
    }
}
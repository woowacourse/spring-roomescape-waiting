package roomescape.reservation.domain;

public record MyReservation(
        Reservation reservation,
        int waitingOrder
) {
    public static MyReservation of(Reservation reservation, ReservationWaitings waitings) {
        if (reservation.getStatus() != Status.WAITING) {
            return new MyReservation(reservation, 0);
        }
        return new MyReservation(reservation, waitings.order(reservation.getId()));
    }
}

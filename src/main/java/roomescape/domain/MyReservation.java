package roomescape.domain;

public record MyReservation(
        Reservation reservation,
        Long waitingNumber,
        ReservationType reservationType
) {
    public static MyReservation reserved(Reservation reservation) {
        return new MyReservation(reservation, null, ReservationType.CONFIRMED);
    }

    public static MyReservation waiting(Reservation reservation, long waitingNumber) {
        return new MyReservation(reservation, waitingNumber, ReservationType.WAITING);
    }
}

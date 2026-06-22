package roomescape.domain;

public record MyReservation(
        Reservation reservation,
        Long waitingNumber,
        ReservationType reservationType,
        ReservationPayment payment
) {
    public static MyReservation reserved(Reservation reservation) {
        return new MyReservation(reservation, null, ReservationType.CONFIRMED, null);
    }

    public static MyReservation waiting(Reservation reservation, long waitingNumber) {
        return new MyReservation(reservation, waitingNumber, ReservationType.WAITING, null);
    }

    public static MyReservation payment(ReservationPayment payment) {
        return new MyReservation(payment.getReservation(), null, ReservationType.PAYMENT, payment);
    }

    public MyReservation withPayment(ReservationPayment payment) {
        return new MyReservation(reservation, waitingNumber, reservationType, payment);
    }
}

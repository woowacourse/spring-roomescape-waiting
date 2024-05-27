package roomescape.domain;

public class ReservationWithWaitingOrder {

    private final Reservation reservation;
    private final Long waitingOrder;

    public ReservationWithWaitingOrder(Reservation reservation, Long waitingOrder) {
        this.reservation = reservation;
        this.waitingOrder = waitingOrder;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Long getWaitingOrder() {
        return waitingOrder;
    }
}

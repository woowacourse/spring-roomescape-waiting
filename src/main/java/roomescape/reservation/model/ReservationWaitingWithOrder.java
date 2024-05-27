package roomescape.reservation.model;

public class ReservationWaitingWithOrder {

    private final ReservationWaiting reservationWaiting;
    private final int order;

    public ReservationWaitingWithOrder(final ReservationWaiting reservationWaiting, final int order) {
        this.reservationWaiting = reservationWaiting;
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public ReservationWaiting getReservationWaiting() {
        return reservationWaiting;
    }
}

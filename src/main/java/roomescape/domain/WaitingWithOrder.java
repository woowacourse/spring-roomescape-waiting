package roomescape.domain;

public class WaitingWithOrder {

    private final ReservationWaiting waiting;
    private final int order;

    public WaitingWithOrder(ReservationWaiting waiting, int order) {
        this.waiting = waiting;
        this.order = order;
    }

    public ReservationWaiting getWaiting() {
        return waiting;
    }

    public int getOrder() {
        return order;
    }
}

package roomescape.domain;

public record ReservationWithWaitingOrder(Reservation reservation, int waitingOrder) {

    public boolean isReserved() {
        return reservation.isReserved();
    }

    public boolean isWaiting() {
        return reservation.isWaiting();
    }
}

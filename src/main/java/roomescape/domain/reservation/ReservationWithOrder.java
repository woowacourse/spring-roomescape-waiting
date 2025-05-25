package roomescape.domain.reservation;

public record ReservationWithOrder(
    Reservation reservation,
    int order
) {

    public boolean isWaiting() {
        return reservation.isWaiting();
    }
}

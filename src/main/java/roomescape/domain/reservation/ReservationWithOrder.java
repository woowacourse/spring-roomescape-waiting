package roomescape.domain.reservation;

public record ReservationWithOrder(
    Reservation reservation,
    int order
) {

    private static final int NOT_WAITING = 1;

    public ReservationWithOrder(final Reservation reservation) {
        this(reservation, NOT_WAITING);
    }

    public boolean isWaiting() {
        return reservation.isWaiting();
    }
}

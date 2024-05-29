package roomescape.reservation.domain;

public class WaitingReservation {
    private static final int MAX_RESERVATION_NUMBER_IN_TIME_SLOT = 1;

    private final Reservation reservation;
    private final long previousCount;

    public WaitingReservation(Reservation reservation, long previousCount) {
        this.reservation = reservation;
        this.previousCount = previousCount;
    }

    public long calculateOrder() {
        return previousCount + MAX_RESERVATION_NUMBER_IN_TIME_SLOT;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public long getPreviousCount() {
        return previousCount;
    }
}

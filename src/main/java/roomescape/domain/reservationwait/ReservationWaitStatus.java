package roomescape.domain.reservationwait;

public enum ReservationWaitStatus {

    WAITING,
    CONFIRMED,
    CANCELED;

    public boolean isWaiting() {
        return this == WAITING;
    }
}

package roomescape.domain;

public enum ReservationWaitStatus {

    WAITING,
    CONFIRMED,
    CANCELED;

    public boolean isWaiting() {
        return this == WAITING;
    }
}

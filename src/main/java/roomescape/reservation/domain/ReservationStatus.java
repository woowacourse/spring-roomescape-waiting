package roomescape.reservation.domain;

public enum ReservationStatus {

    CONFIRMED,
    WAITING;

    public boolean isConfirmed() {
        return this == CONFIRMED;
    }

    public boolean isWaiting() {
        return this == WAITING;
    }
}

package roomescape.reservation.domain;

public enum ReservationStatus {

    PENDING,
    CONFIRMED,
    WAITING;

    public boolean isPending() {
        return this == PENDING;
    }

    public boolean isConfirmed() {
        return this == CONFIRMED;
    }

    public boolean isWaiting() {
        return this == WAITING;
    }

    public boolean isOccupying() {
        return this == CONFIRMED || this == PENDING;
    }
}

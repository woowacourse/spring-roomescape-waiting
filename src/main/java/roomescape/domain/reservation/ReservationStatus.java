package roomescape.domain.reservation;

public enum ReservationStatus {

    RESERVED, WAITING;

    public boolean isWaiting() {
        return this == WAITING;
    }
}

package roomescape.reservation.domain;

public enum ReservationStatus {
    RESERVED,
    WAITING;

    public boolean isReserved() {
        return this == RESERVED;
    }
}

package roomescape.domain;

public enum ReservationStatus {

    RESERVED,
    WAITING;

    public boolean isWaiting() {
        return WAITING.equals(this);
    }
}

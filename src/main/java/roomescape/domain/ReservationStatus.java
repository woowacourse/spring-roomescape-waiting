package roomescape.domain;

public enum ReservationStatus {
    RESERVED("예약"),
    WAITING("대기");

    private final String message;

    ReservationStatus(final String message) {
        this.message = message;
    }

    public boolean isWaiting() {
        return this == WAITING;
    }

    public String getMessage() {
        return message;
    }
}

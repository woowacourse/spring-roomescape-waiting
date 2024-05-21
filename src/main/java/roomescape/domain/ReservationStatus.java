package roomescape.domain;

public enum ReservationStatus {
    RESERVED("예약"),
    WAITING("예약대기");

    private final String message;

    ReservationStatus(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

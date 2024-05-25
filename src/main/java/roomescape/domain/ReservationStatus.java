package roomescape.domain;

public enum ReservationStatus {
    RESERVED("예약");

    private final String message;

    ReservationStatus(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

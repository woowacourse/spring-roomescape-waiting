package roomescape.domain;

public enum ReservationStatus {

    BOOKING("예약"),
    WAITING("예약 대기"),
    ;

    private final String message;

    ReservationStatus(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getMessage(Long rank) {
        return rank + "번째 " + message;
    }
}

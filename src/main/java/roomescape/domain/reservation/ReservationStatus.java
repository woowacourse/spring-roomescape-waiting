package roomescape.domain.reservation;

public enum ReservationStatus {
    RESERVED("예약"),
    WAITING("대기"),
    ;

    private String message;

    ReservationStatus(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

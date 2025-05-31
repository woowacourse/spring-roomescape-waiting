package roomescape.reservation.domain;

public enum ReservationStatus {
    CONFIRMED("예약"),
    WAITING("예약대기"),
    CANCELED("예약취소"),
    WAITING_CANCELED("예약대기취소"),
    ;

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

package roomescape.reservation.domain;

public enum ReservationStatus {
    WAITING("대기"),
    CONFIRMED("예약"),
    DONE("완료"),
    ;

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

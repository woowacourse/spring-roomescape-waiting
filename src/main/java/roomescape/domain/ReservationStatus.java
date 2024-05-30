package roomescape.domain;

public enum ReservationStatus {
    BOOKED("예약"),
    WAITING("예약대기"),
    DENY("거절됨");

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

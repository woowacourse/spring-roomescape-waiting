package roomescape.domain;

public enum ReservationStatus {
    RESERVED("예약"),
    WAITING("예약대기"),
    CANCELED("취소됨");

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

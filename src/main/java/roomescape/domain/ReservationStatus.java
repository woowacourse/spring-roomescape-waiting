package roomescape.domain;

public enum ReservationStatus {
    RESERVED("예약"),
    CANCELED("취소됨"),
    COMPLETED("완료됨");

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

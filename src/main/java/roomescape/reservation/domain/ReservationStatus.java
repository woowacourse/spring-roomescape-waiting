package roomescape.reservation.domain;

public enum ReservationStatus {
    RESERVED("예약"),
    COMPLETED("완료");

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

package roomescape.reservation.model;

public enum ReservationStatus {
    PENDING("대기"),
    RESERVATION("예약"),
    END("완료");

    private final String description;

    ReservationStatus(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

package roomescape.reservation.domain;

public enum ReservationStatus {
    RESERVED("예약"),
    WAITING("예약 대기");

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

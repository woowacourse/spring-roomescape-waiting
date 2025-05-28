package roomescape.reservation.domain;

public enum ReservationStatus {
    CONFIRMED("예약");

    private final String description;

    ReservationStatus(final String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

package roomescape.domain.reservation;

public enum ReservationStatus {
    RESERVED("예약"),
    WAITING("대기");

    private final String description;

    ReservationStatus(final String description) {
        this.description = description;
    }

    public String description() {
        return description;
    }
}

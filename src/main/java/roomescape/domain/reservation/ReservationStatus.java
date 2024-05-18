package roomescape.domain.reservation;

public enum ReservationStatus {
    BOOKED("예약"),
    WAITING("예약대기");

    private final String description;

    ReservationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

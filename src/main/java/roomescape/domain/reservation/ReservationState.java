package roomescape.domain.reservation;

public enum ReservationState {

    RESERVED("예약");

    private final String description;

    ReservationState(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

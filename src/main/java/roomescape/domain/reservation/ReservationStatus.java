package roomescape.domain.reservation;

public enum ReservationStatus {
    RESERVATION("예약"),
    WAITING("대기");

    private final String value;

    ReservationStatus(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

package roomescape.domain.reservation;

public enum ReservationStatus {
    RESERVED("예약");

    private final String value;

    ReservationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

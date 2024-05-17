package roomescape.domain.reservation;

public enum ReservationStatus {
    RESERVED("예약"), WAIT("예약대기");

    private final String value;

    ReservationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

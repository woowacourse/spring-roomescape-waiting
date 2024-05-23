package roomescape.domain.reservation.domain;

public enum Status {
    RESERVATION("예약"), RESERVATION_WAIT("예약대기");

    private final String value;

    Status(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

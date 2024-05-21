package roomescape.reservation.domain;

public enum Status {

    RESERVATION("예약");

    private final String value;

    Status(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

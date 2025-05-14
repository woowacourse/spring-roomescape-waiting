package roomescape.reservation.domain;

public enum Status {

    CONFIRMATION("예약");

    private final String value;

    Status(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

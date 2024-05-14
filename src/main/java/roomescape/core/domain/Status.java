package roomescape.core.domain;

public enum Status {
    BOOKED("예약"),
    STANDBY("대기");

    private final String value;

    Status(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

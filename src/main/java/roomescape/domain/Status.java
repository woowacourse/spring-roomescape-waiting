package roomescape.domain;

public enum Status {

    RESERVED("예약"),
    WAITING("예약대기");

    private final String value;

    Status(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

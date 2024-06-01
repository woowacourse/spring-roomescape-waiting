package roomescape.reservation.domain;

public enum Status {

    RESERVED("예약"),
    WAITING("예약대기");

    private final String value;

    Status(String value) {
        this.value = value;
    }

    public boolean isReserved() {
        return this == RESERVED;
    }

    public boolean isWaiting() {
        return this == WAITING;
    }

    public String getValue() {
        return value;
    }
}

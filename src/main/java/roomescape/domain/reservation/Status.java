package roomescape.domain.reservation;

public enum Status {
    RESERVATION("예약"),
    WAITING("예약대기")
    ;

    private final String value;

    Status(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

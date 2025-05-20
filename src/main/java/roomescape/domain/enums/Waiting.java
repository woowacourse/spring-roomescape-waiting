package roomescape.domain.enums;

public enum Waiting {

    WAITING("대기"),
    CONFIRMED("예약"),
    ;

    private final String value;

    Waiting(String value) {
        this.value = value;
    }

    public boolean isWaiting() {
        return this == WAITING;
    }

    public String getValue() {
        return value;
    }
}

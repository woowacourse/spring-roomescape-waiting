package roomescape.domain.enums;

public enum ReservationStatus {
    CONFIRMED("예약"),
    WAITING("대기"),
    ;

    private final String value;

    ReservationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean isWaiting() {
        return this == WAITING;
    }
}

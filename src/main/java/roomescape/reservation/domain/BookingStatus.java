package roomescape.reservation.domain;

public enum BookingStatus {

    RESERVED("예약"),
    WAITING("대기");

    private final String value;

    BookingStatus(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

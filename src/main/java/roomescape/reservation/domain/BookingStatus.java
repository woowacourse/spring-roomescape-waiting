package roomescape.reservation.domain;

public enum BookingStatus {

    CONFIRMATION("예약");

    private final String value;

    BookingStatus(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

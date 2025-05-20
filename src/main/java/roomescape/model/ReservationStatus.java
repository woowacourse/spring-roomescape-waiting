package roomescape.model;

public enum ReservationStatus {
    RESERVED("예약중");

    private final String value;

    ReservationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

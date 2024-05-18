package roomescape.service.dto;

public enum ReservationStatus {
    BOOKED("예약"), WAIT("예약대기");

    private final String value;

    ReservationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

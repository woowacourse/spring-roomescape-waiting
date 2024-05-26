package roomescape.domain.reservation.domain;

public enum ReservationStatus {
    RESERVATION("예약"), RESERVATION_WAIT("예약대기");

    private final String value;

    ReservationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public boolean isReservation() {
        return this == RESERVATION;
    }
}

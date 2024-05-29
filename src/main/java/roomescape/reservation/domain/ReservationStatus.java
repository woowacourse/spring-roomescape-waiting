package roomescape.reservation.domain;

public enum ReservationStatus {
    BOOKING("예약"),
    WAITING("예약대기");

    private final String status;

    ReservationStatus(final String status) {
        this.status = status;
    }

    public boolean isBooking() {
        return this == BOOKING;
    }

    public String getStatus() {
        return status;
    }
}

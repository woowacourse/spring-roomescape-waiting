package roomescape.reservation.domain;

public enum ReservationStatus {
    BOOKING,
    WAITING;

    public boolean isBooking() {
        return this == BOOKING;
    }

    public boolean isWaiting() {
        return this == WAITING;
    }
}

package roomescape.domain.reservation;

public enum BookStatus {
    WAITING,
    BOOKED,
    WAITING_CANCELLED,
    BOOKING_CANCELLED,
    ;

    public boolean isNotBooked() {
        return this != BOOKED;
    }

    public boolean isNotWaiting() {
        return this != WAITING;
    }
}

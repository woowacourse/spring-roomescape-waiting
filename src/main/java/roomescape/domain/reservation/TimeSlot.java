package roomescape.domain.reservation;

public record TimeSlot(ReservationTime reservationTime, boolean isBooked) {

    public boolean isAvailable() {
        return !isBooked;
    }
}

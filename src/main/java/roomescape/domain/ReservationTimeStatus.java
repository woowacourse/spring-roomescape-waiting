package roomescape.domain;

public class ReservationTimeStatus {

    private final ReservationTime reservationTime;
    private final boolean reserved;

    public ReservationTimeStatus(ReservationTime reservationTime, boolean reserved) {
        this.reservationTime = reservationTime;
        this.reserved = reserved;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public boolean isReserved() {
        return reserved;
    }
}

package roomescape.domain.reservation;

public class ReservationTimeStatus {

    private final ReservationTime reservationTime;
    private final ReservationStatus reservationStatus;

    public ReservationTimeStatus(ReservationTime reservationTime, ReservationStatus reservationStatus) {
        this.reservationTime = reservationTime;
        this.reservationStatus = reservationStatus;
    }

    public ReservationTime getReservationTime() {
        return reservationTime;
    }

    public ReservationStatus getReservationStatus() {
        return reservationStatus;
    }
}

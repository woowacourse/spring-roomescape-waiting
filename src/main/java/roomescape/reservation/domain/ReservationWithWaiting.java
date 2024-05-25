package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationWithWaiting {

    private final Reservation reservation;
    private final int waitingNumber;

    public ReservationWithWaiting(Reservation reservation, int waitingNumber) {
        this.reservation = reservation;
        this.waitingNumber = waitingNumber;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public Long getReservationId() {
        return reservation.getId();
    }

    public String getThemeName() {
        return reservation.getThemeName();
    }

    public LocalDate getReservationDate() {
        return reservation.getDate();
    }

    public LocalTime getStartAt() {
        return reservation.getStartAt();
    }

    public int getWaitingNumber() {
        return waitingNumber;
    }
}

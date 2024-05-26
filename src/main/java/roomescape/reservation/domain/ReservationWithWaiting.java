package roomescape.reservation.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class ReservationWithWaiting {

    private final Reservation reservation;
    private final int waitingNumber;

    public ReservationWithWaiting(Reservation reservation, int waitingNumber) {
        this.reservation = reservation;
        this.waitingNumber = waitingNumber;
    }

    public ReservationWithWaiting(Reservation reservation, Long waitingNumber) {
        this(reservation, waitingNumber.intValue());
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationWithWaiting that = (ReservationWithWaiting) o;
        return waitingNumber == that.waitingNumber && Objects.equals(reservation, that.reservation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservation, waitingNumber);
    }
}

package roomescape.reservation.domain;

import java.time.LocalDate;

public class ReservationDate {

    private final LocalDate reservationDate;

    public ReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public LocalDate getDate() {
        return reservationDate;
    }
}

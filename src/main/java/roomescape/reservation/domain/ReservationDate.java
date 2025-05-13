package roomescape.reservation.domain;

import java.time.LocalDate;

public final class ReservationDate {
    private final LocalDate reservationDate;

    public ReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public LocalDate getReservationDate() {
        return reservationDate;
    }
}

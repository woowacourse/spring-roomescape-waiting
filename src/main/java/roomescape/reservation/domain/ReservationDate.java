package roomescape.reservation.domain;

import jakarta.persistence.Embeddable;
import java.time.LocalDate;

@Embeddable
public class ReservationDate {

    private LocalDate reservationDate;

    public ReservationDate() {

    }

    public ReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    public LocalDate getDate() {
        return reservationDate;
    }
}

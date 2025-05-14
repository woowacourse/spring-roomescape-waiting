package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationDate {

    @Column(name = "date")
    private LocalDate date;

    public ReservationDate(LocalDate reservationDate) {
        this.date = reservationDate;
    }

    public LocalDate getDate() {
        return date;
    }
}

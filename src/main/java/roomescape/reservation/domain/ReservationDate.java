package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;

@Embeddable
public record ReservationDate(
        @Column(name = "date") LocalDate date
) {
}

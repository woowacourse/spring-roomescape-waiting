package roomescape.reservation.domain;

import jakarta.persistence.Embeddable;
import java.time.LocalDate;

@Embeddable
public record ReservationDate(
        LocalDate date
) {
}

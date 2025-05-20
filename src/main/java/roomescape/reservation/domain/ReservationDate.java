package roomescape.reservation.domain;

import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ReservationDate {

    private LocalDate date;

    public ReservationDate(LocalDate date) {
        this.date = date;
    }
}

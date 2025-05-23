package roomescape.domain.reservation.schdule;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public record ReservationDate(
        @Column(nullable = false)
        LocalDate date
) {

    public ReservationDate {
        Objects.requireNonNull(date, "date는 null일 수 없습니다.");
    }

    public ReservationDate(final String date) {
        this(LocalDate.parse(Objects.requireNonNull(date, "date는 null일 수 없습니다.")));
    }
}

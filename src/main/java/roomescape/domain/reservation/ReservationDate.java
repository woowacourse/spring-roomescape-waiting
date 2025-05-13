package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.Objects;

public class ReservationDate {
    private final LocalDate date;

    public ReservationDate(final LocalDate date) {
        this.date = Objects.requireNonNull(date, "date는 null일 수 없습니다.");
    }

    public ReservationDate(final String date) {
        this.date = LocalDate.parse(Objects.requireNonNull(date, "date는 null일 수 없습니다."));
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ReservationDate that)) {
            return false;
        }
        return Objects.equals(getDate(), that.getDate());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getDate());
    }
}

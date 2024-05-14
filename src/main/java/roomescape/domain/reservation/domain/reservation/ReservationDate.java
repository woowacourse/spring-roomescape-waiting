package roomescape.domain.reservation.domain.reservation;

import java.time.LocalDate;
import java.util.Objects;
import roomescape.global.exception.EscapeApplicationException;

public class ReservationDate {

    private final LocalDate date;

    public ReservationDate(LocalDate date) {
        validate(date);
        this.date = date;
    }

    private void validate(LocalDate date) {
        validateNonNull(date);
    }

    private void validateNonNull(LocalDate date) {
        if (date == null) {
            throw new EscapeApplicationException("날짜는 null일 수 없습니다");
        }
    }

    public LocalDate getDate() {
        return date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationDate that = (ReservationDate) o;
        return Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date);
    }
}

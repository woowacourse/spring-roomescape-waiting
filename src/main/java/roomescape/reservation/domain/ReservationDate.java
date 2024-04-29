package roomescape.reservation.domain;

import java.time.LocalDate;
import java.util.Objects;

public class ReservationDate {

    private final LocalDate date;

    public ReservationDate(LocalDate date) {
        this.date = date;
    }

    public boolean isBefore(LocalDate target) {
        return date.isBefore(target);
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

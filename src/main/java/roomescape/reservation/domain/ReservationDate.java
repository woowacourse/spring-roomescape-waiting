package roomescape.reservation.domain;

import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class ReservationDate {

    //TODO: date 변수명 value로 변경
    private LocalDate date;

    public ReservationDate() {
    }

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

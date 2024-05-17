package roomescape.reservation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class ReservationDate {

    @Column(name = "date")
    private LocalDate value;

    public ReservationDate(LocalDate value) {
        this.value = value;
    }

    protected ReservationDate() {
    }

    public boolean isBefore(LocalDate target) {
        return value.isBefore(target);
    }

    public LocalDate getValue() {
        return value;
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
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}

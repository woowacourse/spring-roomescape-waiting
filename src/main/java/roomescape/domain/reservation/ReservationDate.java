package roomescape.domain.reservation;

import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class ReservationDate {
    private LocalDate value;

    protected ReservationDate() {
    }

    private ReservationDate(LocalDate value) {
        this.value = value;
    }

    public static ReservationDate of(LocalDate date) {
        return new ReservationDate(date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ReservationDate that = (ReservationDate) o;
        return Objects.equals(value, that.value);
    }

    public LocalDate getValue() {
        return value;
    }
}

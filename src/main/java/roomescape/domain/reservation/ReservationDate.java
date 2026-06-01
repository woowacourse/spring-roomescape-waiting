package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.Objects;

public class ReservationDate {
    private final LocalDate value;

    public ReservationDate(LocalDate value) {
        this.value = Objects.requireNonNull(value);
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

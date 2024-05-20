package roomescape.reservation.domain;

import jakarta.persistence.Column;
import java.time.LocalDate;
import java.util.Objects;
import roomescape.global.exception.DomainValidationException;

public class ReservationDate {

    @Column(name = "date", nullable = false)
    private LocalDate value;

    public ReservationDate(LocalDate value) {
        validateNotNull(value);
        this.value = value;
    }

    protected ReservationDate() {
    }

    public void validateNotNull(LocalDate value) {
        if (value == null) {
            throw new DomainValidationException("예약 날짜는 필수 입니다");
        }
    }

    public boolean isPast() {
        return value.isBefore(LocalDate.now());
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

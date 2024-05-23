package roomescape.reservation.domain;

import jakarta.persistence.Column;
import java.time.LocalDate;
import java.util.Objects;
import roomescape.global.exception.IllegalRequestException;

public class ReservationDate {

    @Column(name = "date", nullable = false)
    private LocalDate value;

    public ReservationDate(LocalDate value) {
        validate(value);
        this.value = value;
    }

    protected ReservationDate() {
    }

    private void validate(LocalDate value) {
        validateNotNull(value);
        validateNotPast(value);
    }

    public void validateNotNull(LocalDate value) {
        if (value == null) {
            throw new IllegalRequestException("예약 날짜는 필수 입니다");
        }
    }

    public void validateNotPast(LocalDate value) {
        if (value.isBefore(LocalDate.now())) {
            throw new IllegalRequestException("예약 날짜는 과거일 수 없습니다");
        }
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

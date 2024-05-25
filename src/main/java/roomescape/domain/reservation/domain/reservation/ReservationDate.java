package roomescape.domain.reservation.domain.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.time.LocalDate;
import java.util.Objects;
import roomescape.global.exception.ValueNullOrEmptyException;

@Embeddable
public class ReservationDate {

    protected static final String RESERVATION_DATE_NULL_ERROR_MESSAGE = "예약 날짜는 비어있을 수 없습니다.";

    public ReservationDate() {

    }

    @Column(name = "date", nullable = false)
    private LocalDate value;

    public ReservationDate(LocalDate value) {
        validateNullAndBlank(value);
        this.value = value;
    }

    private void validateNullAndBlank(LocalDate value) {
        if (value == null) {
            throw new ValueNullOrEmptyException(RESERVATION_DATE_NULL_ERROR_MESSAGE);
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

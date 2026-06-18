package roomescape.domain.reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;
import java.util.Objects;

import static roomescape.domain.DomainErrorCode.INVALID_INPUT;
import static roomescape.domain.DomainPreconditions.requireNonNull;

@Embeddable
public class ReservationDate {

    @Column(nullable = false)
    private LocalDate date;

    protected ReservationDate() {
    }

    public ReservationDate(LocalDate date) {
        this.date = requireNonNull(date, INVALID_INPUT, "예약 날짜는 비어있을 수 없습니다.");
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

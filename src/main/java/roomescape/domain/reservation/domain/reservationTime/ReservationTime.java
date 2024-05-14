package roomescape.domain.reservation.domain.reservationTime;

import java.time.LocalTime;
import java.util.Objects;
import roomescape.global.exception.EscapeApplicationException;

public class ReservationTime {
    private final Long id;
    private final LocalTime startAt;

    public ReservationTime(Long id, LocalTime startAt) {
        validateNonNull(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    private void validateNonNull(LocalTime startAt) {
        if (startAt == null) {
            throw new EscapeApplicationException("예약 가능 시각은 null일 수 없습니다");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationTime that = (ReservationTime) o;
        return Objects.equals(id, that.id) && Objects.equals(startAt, that.startAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startAt);
    }

    @Override
    public String toString() {
        return "ReservationTime{" +
                "id=" + id +
                ", startAt=" + startAt +
                '}';
    }
}

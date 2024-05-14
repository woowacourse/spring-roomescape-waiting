package roomescape.reservation.domain;

import java.time.LocalTime;
import java.util.Objects;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

public class ReservationTime {
    private final Long id;
    private LocalTime startAt;

    public ReservationTime(Long id, LocalTime startAt) {
        validate(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTime(LocalTime startAt) {
        this(null, startAt);
    }

    public void validate(LocalTime startAt) {
        if (startAt == null) {
            throw new BusinessException(ErrorType.MISSING_REQUIRED_VALUE_ERROR);
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationTime reservationTime = (ReservationTime) o;
        return Objects.equals(id, reservationTime.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ReservationTime{" +
                "id=" + id +
                ", startAt=" + startAt +
                '}';
    }
}

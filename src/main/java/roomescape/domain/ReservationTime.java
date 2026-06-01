package roomescape.domain;

import java.time.LocalTime;
import java.util.Objects;
import roomescape.exception.code.ReservationTimeErrorCode;
import roomescape.exception.domain.ReservationTimeException;

public class ReservationTime {

    private static final LocalTime OPEN_TIME = LocalTime.of(10, 0);
    private static final LocalTime LAST_TIME = LocalTime.of(22, 0);

    private Long id;
    private final LocalTime startAt;

    public ReservationTime(LocalTime startAt) {
        this(null, startAt);
    }

    public ReservationTime(Long id, LocalTime startAt) {
        validate(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    private void validate(LocalTime startAt) {
        validateRange(startAt);
        validateUnit(startAt);
    }

    private void validateRange(LocalTime startAt) {
        if (startAt.isBefore(OPEN_TIME) || startAt.isAfter(LAST_TIME)) {
            throw new ReservationTimeException(ReservationTimeErrorCode.INVALID_RESERVATION_TIME_RANGE);
        }
    }

    private void validateUnit(LocalTime startAt) {
        if (startAt.getMinute() != 0) {
            throw new ReservationTimeException(ReservationTimeErrorCode.INVALID_RESERVATION_TIME_UNIT);
        }
    }

    public ReservationTime createWithId(long id) {
        return new ReservationTime(id, this.startAt);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        ReservationTime reservationTime = (ReservationTime) object;
        if (id != null && reservationTime.id != null) {
            return Objects.equals(id, reservationTime.id);
        }
        return Objects.equals(startAt, reservationTime.startAt);
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(startAt);
    }
}

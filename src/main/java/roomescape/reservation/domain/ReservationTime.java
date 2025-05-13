package roomescape.reservation.domain;

import java.time.LocalTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import roomescape.common.exception.ReservationException;

@Getter
@EqualsAndHashCode(of = {"id"})
public class ReservationTime {

    private static final LocalTime OPEN_TIME = LocalTime.of(10, 0);
    private static final LocalTime CLOSE_TIME = LocalTime.of(22, 0);

    private final Long id;
    private final LocalTime startAt;

    public ReservationTime(final Long id, final LocalTime startAt) {
        validateStartAt(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTime(final LocalTime startAt) {
        this(null, startAt);
    }

    private void validateStartAt(final LocalTime startAt) {
        validateNull(startAt);
        validateTimeRange(startAt);
    }

    private void validateNull(LocalTime startAt) {
        if (startAt == null) {
            throw new ReservationException("time cannot be null");
        }
    }

    private void validateTimeRange(final LocalTime startAt) {
        if (startAt.isBefore(OPEN_TIME) || startAt.isAfter(CLOSE_TIME)) {
            throw new ReservationException("Invalid time range. startAt : " + startAt);
        }
    }
}

package roomescape.domain;

import lombok.Getter;

import java.time.LocalTime;

import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

@Getter
public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;
    private final LocalTime endAt;

    private ReservationTime(final Long id, final LocalTime startAt, final LocalTime endAt) {
        this.id = id;
        validateStartAt(startAt);
        this.startAt = startAt;
        validateEndAt(endAt);
        this.endAt = endAt;
    }

    private static void validateId(final Long id) {
        if (id == null) {
            throw new BusinessException(ErrorCode.TIME_ID_NULL);
        }
    }

    private static void validateStartAt(final LocalTime startAt) {
        if (startAt == null) {
            throw new BusinessException(ErrorCode.START_TIME_NULL);
        }
    }

    private static void validateEndAt(final LocalTime endAt) {
        if (endAt == null) {
            throw new BusinessException(ErrorCode.END_TIME_NULL);
        }
    }

    public static ReservationTime create(final LocalTime startAt, final LocalTime endAt) {
        return new ReservationTime(
                null,
                startAt,
                endAt
        );
    }

    public static ReservationTime createWithId(
            final Long id,
            final LocalTime startAt,
            final LocalTime endAt
    ) {
        validateId(id);
        return new ReservationTime(id, startAt, endAt);
    }

    public ReservationTime withId(final Long id) {
        validateId(id);
        return new ReservationTime(
                id,
                this.startAt,
                this.endAt
        );
    }

    public boolean isBefore() {
        return startAt.isBefore(LocalTime.now());
    }
}

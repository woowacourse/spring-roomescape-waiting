package roomescape.domain;

import java.time.LocalTime;

import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;

    public ReservationTime(Long id, LocalTime startAt) {
        validateStartAt(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    private void validateStartAt(LocalTime startAt) {
        if (startAt == null) {
            throw new CustomException(ErrorCode.TIME_START_AT_NULL);
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}

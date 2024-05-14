package roomescape.reservation.domain;

import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.ValidateException;

import java.time.LocalTime;

public class ReservationTime {
    private final Long id;
    private final LocalTime startAt;

    public ReservationTime(final LocalTime startAt) {
        this(null, startAt);
    }

    public ReservationTime(final Long id, final LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;

        validateNull();
    }

    private void validateNull() {
        if (startAt == null) {
            throw new ValidateException(ErrorType.REQUEST_DATA_BLANK,
                    String.format("예약 시간(Time) 생성에 유효하지 않은 값(null)이 입력되었습니다. [values: %s]", this));
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public String toString() {
        return "Time{" +
                "id=" + id +
                ", startAt=" + startAt +
                '}';
    }
}

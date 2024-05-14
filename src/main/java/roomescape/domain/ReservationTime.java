package roomescape.domain;

import java.time.LocalTime;
import roomescape.exception.BadRequestException;

public class ReservationTime {
    private final Long id;
    private final LocalTime startAt;

    public ReservationTime(LocalTime startAt) {
        this(null, startAt);
    }

    public ReservationTime(Long id, LocalTime startAt) {
        validateTime(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    private void validateTime(LocalTime time) {
        if (time == null) {
            throw new BadRequestException("시간에 빈값을 입력할 수 없습니다.");
        }
    }

    public ReservationTime withId(Long id) {
        return new ReservationTime(id, startAt);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}

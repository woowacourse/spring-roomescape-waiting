package roomescape.reservation.domain;

import roomescape.global.exception.ViolationException;

import java.time.LocalTime;

public class ReservationTime {
    private static final int TIME_UNIT = 10;

    private final Long id;
    private final LocalTime startAt;

    public ReservationTime(LocalTime startAt) {
        this(null, startAt);
    }

    public ReservationTime(Long id, ReservationTime time) {
        this.id = id;
        this.startAt = time.startAt;
    }

    public ReservationTime(Long id, LocalTime startAt) {
        validateTimeUnit(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    private void validateTimeUnit(LocalTime time) {
        if (time.getMinute() % TIME_UNIT != 0) {
            throw new ViolationException("예약 시간은 10분 단위입니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}

package roomescape.domain;

import roomescape.exception.ReservationException;

import java.time.LocalTime;

public record ReservationTime(Long id, LocalTime startAt) {

    private static final LocalTime RESERVATION_START_TIME = LocalTime.of(12, 0);
    private static final LocalTime RESERVATION_END_TIME = LocalTime.of(22, 0);

    public ReservationTime(LocalTime startAt) {
        this(null, startAt);
    }

    public ReservationTime {
        if (startAt.isBefore(RESERVATION_START_TIME) || startAt.isAfter(RESERVATION_END_TIME)) {
            throw new ReservationException("해당 시간은 예약 가능 시간이 아닙니다.");
        }
    }
}

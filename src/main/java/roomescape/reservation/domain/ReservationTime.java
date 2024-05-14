package roomescape.reservation.domain;

import java.time.LocalTime;

public class ReservationTime {
    private Long id;

    private LocalTime startAt;

    public ReservationTime(final Long id, final LocalTime startAt) {
        validateStartAt(startAt);
        this.id = id;
        this.startAt = startAt;
    }

    private void validateStartAt(final LocalTime startAt) {
        if (startAt == null) {
            throw new IllegalArgumentException("[ERROR] 예약 가능 시간을 입력해주세요.");
        }
    }

    public ReservationTime(final LocalTime startAt) {
        this(null, startAt);
    }

    public static ReservationTime of(final long id, final String start_at) {
        return new ReservationTime(id, LocalTime.parse(start_at));
    }

    public ReservationTime assignId(final long id) {
        return new ReservationTime(id, startAt);
    }

    public long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}

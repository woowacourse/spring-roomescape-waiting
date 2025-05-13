package roomescape.reservationTime.domain;

import java.time.LocalTime;
import java.util.Objects;

public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;

    private ReservationTime(final Long id, final LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public static ReservationTime createWithoutId(final LocalTime startAt) {
        return new ReservationTime(null, startAt);
    }

    public static ReservationTime createWithId(final Long id, final LocalTime startAt) {
        return new ReservationTime(Objects.requireNonNull(id), startAt);
    }

    public ReservationTime assignId(final Long id) {
        return new ReservationTime(Objects.requireNonNull(id), startAt);
    }

    public boolean isSameTime(final ReservationTime time) {
        return startAt.equals(time.startAt);
    }

    public boolean isBeforeTime(final LocalTime time) {
        return startAt.isBefore(time);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}

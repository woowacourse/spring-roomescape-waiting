package roomescape.reservationtime.model;

import java.time.LocalTime;
import java.util.Objects;

public class ReservationTime {
    private final Long id;
    private final LocalTime time;

    public ReservationTime(final Long id, final LocalTime time) {
        validateReservationTimeIsNull(time);

        this.id = id;
        this.time = time;
    }

    public static ReservationTime of(final Long id, final ReservationTime reservationTime) {
        return new ReservationTime(id, reservationTime.getTime());
    }

    private void validateReservationTimeIsNull(final LocalTime time) {
        if (time == null) {
            throw new IllegalArgumentException("예약 시간 생성 시 시작 시간은 필수입니다.");
        }
    }

    public boolean isNotAfter(final LocalTime time) {
        return this.time.isBefore(time) || isSameStartAt(time);
    }

    public boolean isSameTo(final Long timeId) {
        return Objects.equals(this.id, timeId);
    }

    public boolean isSameStartAt(final LocalTime time) {
        return Objects.equals(this.time, time);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getTime() {
        return time;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationTime that = (ReservationTime) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

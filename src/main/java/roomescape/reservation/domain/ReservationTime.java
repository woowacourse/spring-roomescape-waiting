package roomescape.reservation.domain;

import java.time.LocalTime;
import java.util.Objects;

public class ReservationTime {

    private static final long EMPTY_ID = 0L;

    private final Long id;
    private final LocalTime startAt;

    public ReservationTime(final Long id, final LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
        validateReservationTime();
    }

    public static ReservationTime withoutId(final LocalTime startAt) {
        return new ReservationTime(EMPTY_ID, startAt);
    }

    public void validateReservationTime() {
        if (startAt == null) {
            throw new IllegalArgumentException("startAt cannot be null");
        }
    }

    public boolean existId() {
        return id != EMPTY_ID;
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationTime that = (ReservationTime) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

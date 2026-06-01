package roomescape.domain.reservation;

import java.time.LocalTime;
import java.util.Objects;

public class ReservationTime {
    private final long id;
    private final LocalTime startAt;

    private ReservationTime(long id, LocalTime startAt) {
        this.id = id;
        this.startAt = Objects.requireNonNull(startAt);
    }

    public static ReservationTime of(long id, LocalTime startAt) {
        return new ReservationTime(id, startAt);
    }

    public static ReservationTime of(LocalTime startAt) {
        return new ReservationTime(0L, startAt);
    }

    public long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReservationTime that = (ReservationTime) o;
        return id == that.id && Objects.equals(startAt, that.startAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startAt);
    }
}

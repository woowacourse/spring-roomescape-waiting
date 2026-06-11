package roomescape.domain.reservation;

import java.time.LocalTime;
import java.util.Objects;

public class ReservationTime {
    private static final long TRANSIENT = 0L;
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
        return new ReservationTime(TRANSIENT, startAt);
    }

    public ReservationTime withId(long id) {
        return new ReservationTime(id, startAt);
    }

    public long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}

package roomescape.domain;

import java.time.LocalTime;

public class ReservationTime {

    private Long id;
    private final LocalTime startAt;

    private ReservationTime(final long id, final LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTime(final long id, final String input) {
        this.id = id;
        this.startAt = LocalTime.parse(input);
    }

    public ReservationTime(final LocalTime startAt) {
        this.startAt = startAt;
    }

    public static ReservationTime parse(final String input) {
        return new ReservationTime(LocalTime.parse(input));
    }

    public ReservationTime toEntity(long id) {
        return new ReservationTime(id, this.startAt);
    }

    public boolean isSameTime(final ReservationTime other) {
        return this.startAt.equals(other.startAt);
    }

    public long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}

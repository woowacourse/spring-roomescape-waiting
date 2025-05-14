package roomescape.time.domain;

import java.time.LocalTime;

public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;

    private ReservationTime(Long id, LocalTime time) {
        this.id = id;
        this.startAt = time;
    }

    public static ReservationTime open(LocalTime time) {
        return new ReservationTime(null, time);
    }

    public static ReservationTime load(Long id, LocalTime time) {
        return new ReservationTime(id, time);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}

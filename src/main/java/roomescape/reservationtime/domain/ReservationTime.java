package roomescape.reservationtime.domain;

import java.time.LocalTime;

public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;
    private final LocalTime finishAt;

    private ReservationTime(Long id, LocalTime startAt, LocalTime finishAt) {
        this.id = id;
        this.startAt = startAt;
        this.finishAt = finishAt;
    }

    public static ReservationTime restore(Long id, LocalTime startAt, LocalTime finishAt) {
        return new ReservationTime(id, startAt, finishAt);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public LocalTime getFinishAt() {
        return finishAt;
    }
}

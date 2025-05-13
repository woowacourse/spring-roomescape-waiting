package roomescape.domain;

import java.time.LocalTime;

public class ReservationTime {

    private final Long id;
    private final LocalTime startAt;
    private final Boolean alreadyBooked;

    public ReservationTime(Long id, LocalTime startAt, Boolean alreadyBooked) {
        this.id = id;
        this.startAt = startAt;
        this.alreadyBooked = alreadyBooked;
    }

    public ReservationTime(Long id, LocalTime startAt) {
        this(id, startAt, null);
    }

    public ReservationTime(LocalTime startAt) {
        this(null, startAt, null);
    }

    public boolean isBefore(LocalTime time) {
        return startAt.isBefore(time);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public Boolean getAlreadyBooked() {
        return alreadyBooked;
    }
}

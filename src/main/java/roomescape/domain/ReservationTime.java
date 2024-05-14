package roomescape.domain;

import java.time.LocalTime;
import java.util.List;

public class ReservationTime {
    private Long id;
    private LocalTime startAt;

    public ReservationTime(Long id, LocalTime startAt) {
        this.id = id;
        this.startAt = startAt;
    }

    public ReservationTime(Long id) {
        this(id, null);
    }

    public ReservationTime(LocalTime startAt) {
        this(null, startAt);
    }

    public boolean isAlreadyBooked(List<Long> bookedTimeIds) {
        return bookedTimeIds.contains(this.id);
    }

    public Long getId() {
        return id;
    }

    public LocalTime getStartAt() {
        return startAt;
    }
}

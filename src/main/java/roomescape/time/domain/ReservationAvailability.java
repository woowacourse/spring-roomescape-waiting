package roomescape.time.domain;

import java.sql.Time;
import java.time.LocalTime;

public final class ReservationAvailability {

    private Long timeId;
    private LocalTime startAt;
    private boolean isBooked;

    public ReservationAvailability(final Long timeId, final Time startAt, final boolean isBooked) {
        this.timeId = timeId;
        this.startAt = startAt.toLocalTime();
        this.isBooked = isBooked;
    }

    public Long getTimeId() {
        return timeId;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public boolean isBooked() {
        return isBooked;
    }
}

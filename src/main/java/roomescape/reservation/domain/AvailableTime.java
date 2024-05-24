package roomescape.reservation.domain;

import java.time.LocalTime;

public class AvailableTime {

    private final Long timeId;

    private final LocalTime startAt;

    private final boolean alreadyBooked;

    public AvailableTime(final Long timeId, final LocalTime startAt, final boolean alreadyBooked) {
        this.timeId = timeId;
        this.startAt = startAt;
        this.alreadyBooked = alreadyBooked;
    }

    public Long getTimeId() {
        return timeId;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public boolean isAlreadyBooked() {
        return alreadyBooked;
    }
}

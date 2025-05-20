package roomescape.time.domain;

import java.sql.Time;
import java.time.LocalTime;

public final class ReservableTime {

    private long timeId;
    private LocalTime startAt;
    private boolean isBooked;

    public ReservableTime(final long timeId, final Time startAt, final boolean isBooked) {
        this.timeId = timeId;
        this.startAt = startAt.toLocalTime();
        this.isBooked = isBooked;
    }

    public long getTimeId() {
        return timeId;
    }

    public LocalTime getStartAt() {
        return startAt;
    }

    public boolean isBooked() {
        return isBooked;
    }
}

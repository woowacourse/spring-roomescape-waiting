package roomescape.domain;

import java.time.LocalTime;

public class TimeSlot {
    private ReservationTime time;
    private ReservationTimeStatus status;

    public TimeSlot(ReservationTime time, ReservationTimeStatus status) {
        this.time = time;
        this.status = status;
    }

    public Long getTimeId() {
        return time.getId();
    }

    public LocalTime getTimeStartAt() {
        return time.getStartAt();
    }

    public ReservationTimeStatus getStatus() {
        return status;
    }
}

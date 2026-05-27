package roomescape.domain;

import java.time.LocalTime;

public class ReservationTimeStatus {
    private ReservationTime time;
    private ReservationStatus status;

    public ReservationTimeStatus(ReservationTime time, ReservationStatus status) {
        this.time = time;
        this.status = status;
    }

    public Long getTimeId() {
        return time.getId();
    }

    public LocalTime getTimeStartAt() {
        return time.getStartAt();
    }

    public ReservationStatus getStatus() {
        return status;
    }
}

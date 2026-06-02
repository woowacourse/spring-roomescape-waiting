package roomescape.dto.projection;

import java.time.LocalTime;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;

public record ReservationTimeStatusProjection(ReservationTime time, ReservationStatus status) {

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

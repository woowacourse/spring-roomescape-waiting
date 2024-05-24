package roomescape.service.schedule.dto;

import java.time.LocalTime;
import roomescape.config.TimeFormatConstraint;
import roomescape.domain.schedule.ReservationTime;

public record ReservationTimeCreateRequest(
        @TimeFormatConstraint LocalTime startAt
) {
    public ReservationTime toReservationTime() {
        return new ReservationTime(startAt);
    }
}

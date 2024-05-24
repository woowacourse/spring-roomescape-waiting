package roomescape.service.schedule.dto;

import java.time.LocalTime;
import roomescape.config.TimeFormatConstraint;
import roomescape.domain.schedule.ReservationTime;

public record ReservationTimeResponse(
        long id,
        @TimeFormatConstraint LocalTime startAt
) {
    public ReservationTimeResponse(ReservationTime reservationTime) {
        this(reservationTime.getId(), reservationTime.getStartAt());
    }
}

package roomescape.service.schedule.dto;

import java.time.LocalTime;
import roomescape.config.TimeFormatConstraint;

public record AvailableReservationTimeResponse(
        long id,
        @TimeFormatConstraint LocalTime startAt,
        boolean alreadyBooked) {
}

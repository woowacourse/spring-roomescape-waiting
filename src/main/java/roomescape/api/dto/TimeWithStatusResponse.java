package roomescape.api.dto;

import java.time.LocalTime;
import roomescape.domain.projection.ReservationTimeAvailability;

public record TimeWithStatusResponse(
        Long id,
        LocalTime startAt,
        boolean reserved
) {
    public static TimeWithStatusResponse from(ReservationTimeAvailability availability) {
        return new TimeWithStatusResponse(
                availability.time().getId(),
                availability.time().getStartAt(),
                !availability.available()
        );
    }
}

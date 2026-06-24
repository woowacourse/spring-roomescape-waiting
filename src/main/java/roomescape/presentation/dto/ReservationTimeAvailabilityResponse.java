package roomescape.presentation.dto;

import java.time.LocalTime;
import roomescape.domain.projection.ReservationTimeAvailability;

public record ReservationTimeAvailabilityResponse(
        Long id,
        LocalTime startAt,
        boolean reserved
) {
    public static ReservationTimeAvailabilityResponse from(ReservationTimeAvailability availability) {
        return new ReservationTimeAvailabilityResponse(
                availability.time().getId(),
                availability.time().getStartAt(),
                !availability.available()
        );
    }
}

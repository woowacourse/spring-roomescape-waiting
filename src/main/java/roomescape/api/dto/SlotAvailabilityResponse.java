package roomescape.api.dto;

import java.time.LocalTime;
import roomescape.domain.projection.ReservationTimeAvailability;

public record SlotAvailabilityResponse(
        Long id,
        LocalTime startAt,
        boolean reserved
) {
    public static SlotAvailabilityResponse from(ReservationTimeAvailability availability) {
        return new SlotAvailabilityResponse(
                availability.time().getId(),
                availability.time().getStartAt(),
                !availability.available()
        );
    }
}

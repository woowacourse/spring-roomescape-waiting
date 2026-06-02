package roomescape.controller.dto;

import roomescape.domain.TimeAvailability;

public record TimeAvailabilityResponse(
        ReservationTimeResponse time,
        boolean available
) {

    public static TimeAvailabilityResponse from(TimeAvailability timeAvailability) {
        return new TimeAvailabilityResponse(
                ReservationTimeResponse.from(timeAvailability.getTime()),
                timeAvailability.isAvailable()
        );
    }
}

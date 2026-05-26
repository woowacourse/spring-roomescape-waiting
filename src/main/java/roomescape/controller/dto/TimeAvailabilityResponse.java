package roomescape.controller.dto;

import roomescape.service.result.TimeAvailabilityResult;

public record TimeAvailabilityResponse(
        ReservationTimeResponse time,
        boolean available
) {

    public static TimeAvailabilityResponse from(TimeAvailabilityResult result) {
        return new TimeAvailabilityResponse(
                new ReservationTimeResponse(result.timeId(), result.startAt()),
                result.available());
    }
}

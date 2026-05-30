package roomescape.controller.dto.response;

import roomescape.domain.ReservationTime;

public record ReservationTimeAvailabilityResponse(
        ReservationTimeResponse time,
        boolean available
) {
    public static ReservationTimeAvailabilityResponse from(ReservationTime time, boolean available) {
        return new ReservationTimeAvailabilityResponse(ReservationTimeResponse.from(time), available);
    }
}

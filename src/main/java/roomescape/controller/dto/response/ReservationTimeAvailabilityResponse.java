package roomescape.controller.dto.response;

import roomescape.domain.ReservationAvailability;
import roomescape.domain.ReservationTime;

public record ReservationTimeAvailabilityResponse(
        ReservationTimeResponse time,
        ReservationAvailability availability
) {
    public static ReservationTimeAvailabilityResponse from(ReservationTime reservationTime,
                                                           ReservationAvailability availability) {
        return new ReservationTimeAvailabilityResponse(
                ReservationTimeResponse.from(reservationTime),
                availability);
    }
}

package roomescape.service.dto.response;

import roomescape.domain.ReservationAvailability;
import roomescape.domain.ReservationTime;

public record ServiceReservationTimeAvailabilityResponse(
        ServiceReservationTimeResponse time,
        ReservationAvailability availability
) {
    public static ServiceReservationTimeAvailabilityResponse from(ReservationTime time,
                                                                  ReservationAvailability availability) {
        return new ServiceReservationTimeAvailabilityResponse(
                ServiceReservationTimeResponse.from(time),
                availability);
    }
}

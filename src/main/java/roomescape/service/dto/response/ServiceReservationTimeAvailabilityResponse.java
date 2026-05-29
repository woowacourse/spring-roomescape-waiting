package roomescape.service.dto.response;

import roomescape.domain.ReservationTime;

public record ServiceReservationTimeAvailabilityResponse(
        ServiceReservationTimeResponse time,
        boolean available
) {
    public static ServiceReservationTimeAvailabilityResponse from(ReservationTime time, boolean available) {
        return new ServiceReservationTimeAvailabilityResponse(
                ServiceReservationTimeResponse.from(time),
                available);
    }
}

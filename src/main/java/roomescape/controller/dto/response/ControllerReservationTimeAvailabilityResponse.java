package roomescape.controller.dto.response;

import roomescape.domain.ReservationAvailability;
import roomescape.service.dto.response.ServiceReservationTimeAvailabilityResponse;

public record ControllerReservationTimeAvailabilityResponse(
        ControllerReservationTimeResponse time,
        ReservationAvailability availability
) {
    public static ControllerReservationTimeAvailabilityResponse from(
            ServiceReservationTimeAvailabilityResponse response) {
        return new ControllerReservationTimeAvailabilityResponse(
                ControllerReservationTimeResponse.from(response.time()),
                response.availability());
    }
}

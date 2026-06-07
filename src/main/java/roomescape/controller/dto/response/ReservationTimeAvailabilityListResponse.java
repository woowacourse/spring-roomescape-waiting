package roomescape.controller.dto.response;

import java.util.List;

public record ReservationTimeAvailabilityListResponse(
        List<ReservationTimeAvailabilityResponse> items
) {
    public static ReservationTimeAvailabilityListResponse from(
            List<ReservationTimeAvailabilityResponse> availabilityResponses) {
        return new ReservationTimeAvailabilityListResponse(availabilityResponses);
    }
}

package roomescape.presentation.dto;

import java.util.List;

public record ReservationTimeAvailabilityResponses(
        List<ReservationTimeAvailabilityResponse> times
) {
    public static ReservationTimeAvailabilityResponses of(List<ReservationTimeAvailabilityResponse> times) {
        return new ReservationTimeAvailabilityResponses(times);
    }
}

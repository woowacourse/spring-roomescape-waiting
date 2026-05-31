package roomescape.api.dto;

import java.util.List;

public record SlotAvailabilityResponses(
        List<SlotAvailabilityResponse> times
) {
    public static SlotAvailabilityResponses of(List<SlotAvailabilityResponse> times) {
        return new SlotAvailabilityResponses(times);
    }
}

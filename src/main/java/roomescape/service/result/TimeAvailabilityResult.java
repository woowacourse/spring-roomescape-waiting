package roomescape.service.result;

import java.time.LocalTime;

public record TimeAvailabilityResult(
        Long timeId,
        LocalTime startAt,
        boolean available
) {
}

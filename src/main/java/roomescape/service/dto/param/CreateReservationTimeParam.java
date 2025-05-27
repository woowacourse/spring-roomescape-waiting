package roomescape.service.dto.param;

import java.time.LocalTime;

public record CreateReservationTimeParam(
        LocalTime startAt
) {
}

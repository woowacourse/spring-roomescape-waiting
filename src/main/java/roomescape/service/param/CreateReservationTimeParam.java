package roomescape.service.param;

import java.time.LocalTime;

public record CreateReservationTimeParam(
        LocalTime startAt
) {
}

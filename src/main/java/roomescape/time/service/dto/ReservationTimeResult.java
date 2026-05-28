package roomescape.time.service.dto;

import java.time.LocalTime;
import roomescape.time.domain.ReservationTime;

public record ReservationTimeResult(
        Long id,
        LocalTime startAt
) {
    public static ReservationTimeResult from(ReservationTime time) {
        return new ReservationTimeResult(time.id(), time.startAt());
    }
}

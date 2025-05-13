package roomescape.controller.response;

import java.time.LocalTime;
import roomescape.service.result.ReservationTimeResult;

public record ReservationTimeResponse(
        Long id,
        LocalTime startAt
) {
    public static ReservationTimeResponse from(ReservationTimeResult reservationTimeResult) {
        return new ReservationTimeResponse(reservationTimeResult.id(), reservationTimeResult.startAt());
    }
}

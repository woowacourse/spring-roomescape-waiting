package roomescape.presentation.api.reservation.response;

import java.time.LocalTime;
import roomescape.application.reservation.dto.ReservationTimeResult;

public record ReservationTimeResponse(
        Long id,
        LocalTime startAt
) {

    public static ReservationTimeResponse from(ReservationTimeResult reservationTimeResult) {
        return new ReservationTimeResponse(reservationTimeResult.id(), reservationTimeResult.startAt());
    }
}

package roomescape.application.reservation.query.dto;

import java.time.LocalTime;
import roomescape.domain.reservation.ReservationTime;

public record ReservationTimeResult(
        Long id,
        LocalTime startAt
) {

    public static ReservationTimeResult from(ReservationTime reservationTime) {
        return new ReservationTimeResult(reservationTime.getId(), reservationTime.getStartAt());
    }
}

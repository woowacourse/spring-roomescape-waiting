package roomescape.reservationtime.dto.response;

import java.time.LocalTime;
import roomescape.reservationtime.domain.ReservationTime;

public record ReservationTimeResponse(
        Long id,
        LocalTime startAt
) {
    public static ReservationTimeResponse from(final ReservationTime reservationTime) {
        return new ReservationTimeResponse(reservationTime.getId(), reservationTime.getStartAt());
    }
}
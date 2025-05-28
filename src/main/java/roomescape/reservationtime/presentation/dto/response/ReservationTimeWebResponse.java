package roomescape.reservationtime.presentation.dto.response;

import java.time.LocalTime;
import roomescape.reservationtime.domain.ReservationTime;

public record ReservationTimeWebResponse(
        Long id,
        LocalTime startAt
) {
    public static ReservationTimeWebResponse from(final ReservationTime reservationTime) {
        return new ReservationTimeWebResponse(reservationTime.getId(), reservationTime.getStartAt());
    }
}

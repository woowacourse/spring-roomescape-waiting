package roomescape.reservationtime.application.dto.response;

import java.time.LocalTime;
import roomescape.reservationtime.domain.ReservationTime;

public record ReservationTimeSaveResponse(
        Long id,
        LocalTime startAt
) {
    public static ReservationTimeSaveResponse from(ReservationTime reservationTime) {
        return new ReservationTimeSaveResponse(reservationTime.id(), reservationTime.startAt());
    }
}

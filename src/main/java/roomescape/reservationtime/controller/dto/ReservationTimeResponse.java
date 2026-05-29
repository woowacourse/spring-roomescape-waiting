package roomescape.reservationtime.controller.dto;

import java.time.LocalTime;
import roomescape.reservationtime.ReservationTime;

public record ReservationTimeResponse(
        Long id,
        LocalTime startAt
) {

    public static ReservationTimeResponse from(final ReservationTime reservationTime) {
        return new ReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt()
        );
    }
}

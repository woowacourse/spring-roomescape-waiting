package roomescape.reservationtime.controller.dto.response;

import roomescape.reservationtime.domain.ReservationTime;

import java.time.LocalTime;

public record ReservationTimeResponse(
        Long id,
        LocalTime startAt
) {

    public static ReservationTimeResponse from(final ReservationTime time) {
        return new ReservationTimeResponse(
                time.getId(),
                time.getStartAt()
        );
    }
}

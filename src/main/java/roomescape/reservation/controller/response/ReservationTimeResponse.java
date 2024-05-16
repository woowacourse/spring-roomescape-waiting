package roomescape.reservation.controller.response;

import roomescape.reservation.dto.ReservationTimeDto;

import java.time.LocalTime;

public record ReservationTimeResponse(Long id, LocalTime startAt) {
    public static ReservationTimeResponse from(final ReservationTimeDto reservationTime) {
        return new ReservationTimeResponse(reservationTime.id(), reservationTime.startAt());
    }
}

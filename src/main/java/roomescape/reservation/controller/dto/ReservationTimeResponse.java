package roomescape.reservation.controller.dto;

import roomescape.reservation.domain.ReservationTime;

import java.time.LocalTime;

public record ReservationTimeResponse(long id, LocalTime startAt) {
    public static ReservationTimeResponse from(ReservationTime reservationTime) {
        return new ReservationTimeResponse(reservationTime.getId(), reservationTime.getStartAt());
    }
}

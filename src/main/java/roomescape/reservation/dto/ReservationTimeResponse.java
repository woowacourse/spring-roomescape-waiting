package roomescape.reservation.dto;

import roomescape.reservation.model.ReservationTime;

import java.time.LocalTime;

public record ReservationTimeResponse(Long id, LocalTime startAt) {
    public static ReservationTimeResponse from(final ReservationTime reservationTime) {
        return new ReservationTimeResponse(reservationTime.getId(), reservationTime.getStartAt());
    }
}

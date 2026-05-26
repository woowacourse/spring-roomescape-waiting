package roomescape.reservationtime.dto.response;

import roomescape.reservationtime.ReservationTime;

import java.time.LocalTime;

public record ReservationTimeSaveResponse(
        Long id,
        LocalTime startAt
) {
    public static ReservationTimeSaveResponse from(ReservationTime reservationTime) {
        return new ReservationTimeSaveResponse(reservationTime.id(), reservationTime.startAt());
    }
}

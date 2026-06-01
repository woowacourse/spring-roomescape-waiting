package roomescape.reservationtime.controller.dto;

import roomescape.reservationtime.domain.ReservationTime;

public record ReservationTimeResponse(Long id, String startAt) {
    public static ReservationTimeResponse from(ReservationTime reservationTime) {
        return new ReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt().toString()
        );
    }
}

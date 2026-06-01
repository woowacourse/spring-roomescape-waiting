package roomescape.controller.dto;

import roomescape.domain.ReservationTime;

import java.time.LocalTime;

public record ReservationWaitingTimeResponse(
        Long id,
        LocalTime startAt
) {
    public static ReservationWaitingTimeResponse from(ReservationTime time) {
        return new ReservationWaitingTimeResponse(
                time.getId(),
                time.getStartAt());
    }
}

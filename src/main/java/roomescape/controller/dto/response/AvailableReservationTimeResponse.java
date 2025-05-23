package roomescape.controller.dto.response;

import roomescape.service.dto.result.AvailableReservationTimeResult;

import java.time.LocalTime;

public record AvailableReservationTimeResponse(Long timeId, LocalTime startAt, boolean booked) {
    public static AvailableReservationTimeResponse from(AvailableReservationTimeResult availableReservationTimeResult) {
        return new AvailableReservationTimeResponse(
                availableReservationTimeResult.timeId(),
                availableReservationTimeResult.startAt(),
                availableReservationTimeResult.booked()
        );
    }
}

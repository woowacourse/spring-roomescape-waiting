package roomescape.controller.dto;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.service.dto.AvailableTimeResult;

public record AvailableTimeResponse(
        long id,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt,
        boolean isAvailable,
        int waitNumber
) {

    public static AvailableTimeResponse from(AvailableTimeResult result) {
        int reservationCount = result.reservationCount();
        return new AvailableTimeResponse(
                result.id(),
                result.startAt(),
                reservationCount == 0,
                Math.max(reservationCount - 1, 0)
        );
    }
}

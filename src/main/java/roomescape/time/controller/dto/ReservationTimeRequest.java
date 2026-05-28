package roomescape.time.controller.dto;

import roomescape.global.exception.InvalidRequestFormatException;

import java.time.LocalTime;
import roomescape.time.exception.TimeErrorCode;
import roomescape.global.exception.BadRequestException;

import roomescape.time.service.dto.ReservationTimeCommand;

public record ReservationTimeRequest(LocalTime startAt) {

    public ReservationTimeRequest {
        validateNotNull(startAt);
    }

    private void validateNotNull(Object value) {
        if (value == null) {
            throw new InvalidRequestFormatException(TimeErrorCode.INVALID_FORMAT.getMessage());
        }
    }

    public ReservationTimeCommand toCommand() {
        return new ReservationTimeCommand(startAt);
    }
}

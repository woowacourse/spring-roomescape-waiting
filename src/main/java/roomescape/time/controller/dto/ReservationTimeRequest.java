package roomescape.time.controller.dto;

import java.time.LocalTime;
import roomescape.global.exception.BadRequestException;
import roomescape.time.exception.TimeErrorCode;
import roomescape.time.service.dto.ReservationTimeCommand;

public record ReservationTimeRequest(LocalTime startAt) {

    public ReservationTimeRequest {
        validateNotNull(startAt);
    }

    private void validateNotNull(Object value) {
        if (value == null) {
            throw new BadRequestException(TimeErrorCode.INVALID_FORMAT.getMessage());
        }
    }

    public ReservationTimeCommand toCommand() {
        return new ReservationTimeCommand(startAt);
    }
}

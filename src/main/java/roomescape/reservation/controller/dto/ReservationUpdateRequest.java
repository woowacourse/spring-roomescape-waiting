package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import roomescape.global.exception.BadRequestException;
import roomescape.reservation.exception.ReservationErrorCode;

public record ReservationUpdateRequest(
        LocalDate date,
        Long timeId
) {
    public ReservationUpdateRequest {
        if (date == null && timeId == null) {
            throw new BadRequestException(ReservationErrorCode.INVALID_UPDATE_FORMAT);
        }
    }
}

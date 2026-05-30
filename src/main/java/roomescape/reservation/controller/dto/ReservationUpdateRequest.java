package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import roomescape.global.exception.BadRequestException;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.reservation.service.dto.ReservationUpdateCommand;

public record ReservationUpdateRequest(LocalDate date, Long timeId) {

    public ReservationUpdateRequest {
        if (date == null && timeId == null) {
            throw new BadRequestException(ReservationErrorCode.INVALID_FORMAT.getMessage());
        }
    }

    public ReservationUpdateCommand toCommand() {
        return new ReservationUpdateCommand(
                date,
                timeId
        );
    }
}

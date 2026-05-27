package roomescape.reservationWaiting.controller.dto;

import roomescape.global.exception.InvalidRequestFormatException;

import java.time.LocalDate;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.global.exception.BadRequestException;

import roomescape.reservationWaiting.service.dto.ReservationWaitingCommand;

public record ReservationWaitingRequest(String name, LocalDate date, Long timeId, Long themeId) {

    public ReservationWaitingRequest {
        if (name == null || name.isBlank() ||
                date == null || timeId == null || themeId == null) {
            throw new InvalidRequestFormatException(ReservationErrorCode.INVALID_FORMAT.getMessage());
        }
    }

    public ReservationWaitingCommand toCommand() {
        return new ReservationWaitingCommand(
                name,
                date,
                timeId,
                themeId
        );
    }
}

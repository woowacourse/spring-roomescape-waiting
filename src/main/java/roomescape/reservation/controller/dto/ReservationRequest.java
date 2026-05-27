package roomescape.reservation.controller.dto;

import roomescape.global.exception.InvalidRequestFormatException;

import java.time.LocalDate;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.global.exception.BadRequestException;

import roomescape.reservation.service.dto.ReservationCommand;

public record ReservationRequest(String name, LocalDate date, Long timeId, Long themeId) {

    public ReservationRequest {
        if (name == null || name.isBlank() ||
                date == null || timeId == null || themeId == null) {
            throw new InvalidRequestFormatException(ReservationErrorCode.INVALID_FORMAT.getMessage());
        }
    }

    public ReservationCommand toCommand() {
        return new ReservationCommand(
                name,
                date,
                timeId,
                themeId
        );
    }
}

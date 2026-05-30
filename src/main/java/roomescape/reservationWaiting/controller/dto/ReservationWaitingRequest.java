package roomescape.reservationWaiting.controller.dto;

import java.time.LocalDate;
import roomescape.global.exception.InvalidRequestFormatException;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.reservationWaiting.service.dto.ReservationWaitingCommand;

public record ReservationWaitingRequest(
        String name,
        LocalDate date,
        Long timeId,
        Long themeId
) {

    public ReservationWaitingRequest {
        validateString(name);
        validateNotNull(date);
        validateNotNull(timeId);
        validateNotNull(themeId);
    }

    private void validateString(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidRequestFormatException(ReservationErrorCode.INVALID_FORMAT.getMessage());
        }
    }

    private void validateNotNull(Object value) {
        if (value == null) {
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

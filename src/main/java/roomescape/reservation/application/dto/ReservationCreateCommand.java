package roomescape.reservation.application.dto;

import static roomescape.global.validation.ValidationUtils.requireNotBlank;
import static roomescape.global.validation.ValidationUtils.requireNotNull;

import java.time.LocalDate;
import roomescape.global.exception.ReservationErrorCode;

public record ReservationCreateCommand(
        String name,
        LocalDate date,
        Long timeId,
        Long themeId
) {
    public ReservationCreateCommand {
        requireNotBlank(name, ReservationErrorCode.RESERVATION_NAME_REQUIRED);
        requireNotNull(date, ReservationErrorCode.RESERVATION_DATE_REQUIRED);
        requireNotNull(timeId, ReservationErrorCode.RESERVATION_TIME_REQUIRED);
        requireNotNull(themeId, ReservationErrorCode.RESERVATION_THEME_REQUIRED);
    }
}

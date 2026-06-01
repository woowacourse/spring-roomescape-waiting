package roomescape.reservation.application.dto;

import static roomescape.global.validation.ValidationUtils.requireNotBlank;
import static roomescape.global.validation.ValidationUtils.requireNotNull;

import java.time.LocalDate;
import roomescape.global.exception.ReservationErrorCode;


public record ReservationUpdateCommand(
    Long id,
    LocalDate date,
    Long timeId,
    String name
) {
    public ReservationUpdateCommand {
        requireNotNull(id, ReservationErrorCode.RESERVATION_ID_REQUIRED);
        requireNotNull(date, ReservationErrorCode.RESERVATION_DATE_REQUIRED);
        requireNotNull(timeId, ReservationErrorCode.RESERVATION_TIME_REQUIRED);
        requireNotBlank(name, ReservationErrorCode.RESERVATION_NAME_REQUIRED);
    }
}

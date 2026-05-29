package roomescape.reservationTime.application.dto;

import static roomescape.global.validation.ValidationUtils.requireNotNull;

import java.time.LocalTime;
import roomescape.global.exception.ReservationTimeErrorCode;

public record ReservationTimeCreateCommand(
        LocalTime startAt
) {
    public ReservationTimeCreateCommand {
        requireNotNull(startAt, ReservationTimeErrorCode.RESERVATION_TIME_START_AT_REQUIRED);
    }
}

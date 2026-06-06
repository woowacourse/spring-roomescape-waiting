package roomescape.controller.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Wait;
import roomescape.exception.CustomInvalidRequestException;
import roomescape.exception.ErrorCode;

public record ReservationCreateRequest(
        String name,
        LocalDate date,
        Long timeId,
        Long themeId
) {

    public ReservationCreateRequest {
        validate(name, date, timeId, themeId);
    }

    public Reservation toReservation(ReservationTime reservationTime, Theme theme) {
        return new Reservation(name, date, reservationTime, theme);
    }

    public Wait toWait(LocalDateTime createdAt, ReservationTime reservationTime, Theme theme) {
        return new Wait(createdAt, name, date, reservationTime, theme);
    }

    private void validate(String name, LocalDate date, Long timeId, Long themeId) {
        if (name == null || name.isBlank()) {
            throw new CustomInvalidRequestException(ErrorCode.NOT_ALLOW_NAME_NULL);
        }
        if (date == null) {
            throw new CustomInvalidRequestException(ErrorCode.NOT_ALLOW_DATE_NULL);
        }
        if (timeId == null) {
            throw new CustomInvalidRequestException(ErrorCode.NOT_ALLOW_TIME_NULL);
        }
        if (themeId == null) {
            throw new CustomInvalidRequestException(ErrorCode.NOT_ALLOW_THEME_NULL);
        }
    }
}

package roomescape.reservation.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.common.exception.InvalidDateException;
import roomescape.common.exception.InvalidIdException;
import roomescape.common.exception.message.RequestExceptionMessage;

public record UserReservationRequest(
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        Long timeId,
        Long themeId
) {
    public UserReservationRequest {
        if (date == null) {
            throw new InvalidDateException(RequestExceptionMessage.INVALID_DATE.getMessage());
        }
        if (date.isBefore(LocalDate.now())) {
            throw new InvalidDateException(RequestExceptionMessage.DATE_BEFORE_NOW.getMessage());
        }
        if (timeId == null) {
            throw new InvalidIdException(RequestExceptionMessage.INVALID_TIME_ID.getMessage());
        }
        if (themeId == null) {
            throw new InvalidIdException(RequestExceptionMessage.INVALID_THEME_ID.getMessage());
        }
    }
}

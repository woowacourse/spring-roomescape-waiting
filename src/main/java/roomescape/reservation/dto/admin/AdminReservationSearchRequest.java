package roomescape.reservation.dto.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.common.exception.InvalidDateException;
import roomescape.common.exception.InvalidIdException;
import roomescape.common.exception.message.RequestExceptionMessage;

public record AdminReservationSearchRequest(
        Long memberId,
        Long themeId,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dateFrom,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate dateTo
) {
    public AdminReservationSearchRequest {
        if (dateFrom == null || dateTo == null) {
            throw new InvalidDateException(RequestExceptionMessage.INVALID_DATE.getMessage());
        }
        if (memberId == null) {
            throw new InvalidIdException(RequestExceptionMessage.INVALID_MEMBER_ID.getMessage());
        }
        if (themeId == null) {
            throw new InvalidIdException(RequestExceptionMessage.INVALID_THEME_ID.getMessage());
        }
    }
}

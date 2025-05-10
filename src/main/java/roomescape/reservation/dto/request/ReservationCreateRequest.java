package roomescape.reservation.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.global.exception.InvalidInputException;

public record ReservationCreateRequest(
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
    Long timeId,
    Long themeId) {

    public ReservationCreateRequest {
        validateNull(date, timeId, themeId);
    }

    private void validateNull(LocalDate date, Long timeId, Long themeId) {
        if (date == null) {
            throw new InvalidInputException("예약할 날짜가 입력되지 않았다.");
        }
        if (timeId == null) {
            throw new InvalidInputException("예약할 시간이 입력되지 않았다.");
        }
        if (themeId == null) {
            throw new InvalidInputException("예약할 테마가 입력되지 않았다.");
        }
    }
}

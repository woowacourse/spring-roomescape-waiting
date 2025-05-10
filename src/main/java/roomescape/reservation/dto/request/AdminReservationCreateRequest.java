package roomescape.reservation.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.global.exception.InvalidInputException;

import java.time.LocalDate;

public record AdminReservationCreateRequest(
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
    Long memberId,
    Long timeId,
    Long themeId) {

    public AdminReservationCreateRequest {
        validateNull(date, memberId, timeId, themeId);
    }

    private void validateNull(LocalDate date, Long memberId, Long timeId, Long themeId) {
        if (date == null) {
            throw new InvalidInputException("예약할 날짜가 입력되지 않았다.");
        }
        if (memberId == null) {
            throw new InvalidInputException("예약하는 멤버가 누구인지 입력되지 않았다.");
        }
        if (timeId == null) {
            throw new InvalidInputException("예약할 시간이 입력되지 않았다.");
        }
        if (themeId == null) {
            throw new InvalidInputException("예약할 테마가 입력되지 않았다.");
        }
    }
}

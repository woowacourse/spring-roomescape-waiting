package roomescape.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.exception.InvalidInputException;

public record ReservationRequest(
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
    String name,
    Long timeId,
    Long themeId) {

    public ReservationRequest {
        validateNull(date, name, timeId, themeId);
        validateName(name);
    }

    private void validateNull(LocalDate date, String name, Long timeId, Long themeId) {
        if (date == null) {
            throw new InvalidInputException("예약할 날짜가 입력되지 않았다.");
        }
        if (name == null) {
            throw new InvalidInputException("예약자 이름이 입력되지 않았다.");
        }
        if (timeId == null) {
            throw new InvalidInputException("예약할 시간이 입력되지 않았다.");
        }
        if (themeId == null) {
            throw new InvalidInputException("예약할 테마가 입력되지 않았다.");
        }
    }

    private void validateName(String name) {
        if (name.isBlank()) {
            throw new InvalidInputException("예약자 이름은 한 글자 이상이어야 한다.");
        }
    }
}

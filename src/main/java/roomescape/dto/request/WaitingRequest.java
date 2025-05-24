package roomescape.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import roomescape.exception.custom.InvalidInputException;

public record WaitingRequest(
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
    Long timeId,
    Long themeId) {

    public WaitingRequest {
        validateNull(date, timeId, themeId);
    }

    private void validateNull(LocalDate date, Long timeId, Long themeId) {
        if (date == null || timeId == null || themeId == null) {
            throw new InvalidInputException("선택되지 않은 값 존재");
        }
    }
}

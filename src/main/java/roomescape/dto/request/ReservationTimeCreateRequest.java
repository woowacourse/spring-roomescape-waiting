package roomescape.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.exception.InvalidInputException;

public record ReservationTimeCreateRequest(
    @JsonFormat(pattern = "HH:mm") LocalTime startAt) {

    public ReservationTimeCreateRequest {
        validateTime(startAt);
    }

    private void validateTime(LocalTime startAt) {
        if (startAt == null) {
            throw new InvalidInputException("시간이 입력되지 않았다.");
        }
    }
}

package roomescape.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import roomescape.model.ReservationTime;

public record ReservationTimeRegisterDto(
        @NotBlank String startAt
) {

    public ReservationTime convertToTime() {
        try {
            LocalTime startTime = LocalTime.parse(startAt);
            return new ReservationTime(startTime);
        } catch (DateTimeParseException e) {
            throw new IllegalStateException("시간형식이 잘못되었습니다");
        }
    }
}

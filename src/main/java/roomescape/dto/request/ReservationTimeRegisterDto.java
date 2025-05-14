package roomescape.dto.request;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import roomescape.model.ReservationTime;

public record ReservationTimeRegisterDto(
        String startAt
) {
    public ReservationTimeRegisterDto {
        if (startAt == null || startAt.isBlank()) {
            throw new IllegalArgumentException("시작 시각은 null이거나 공백일 수 없습니다");
        }
    }

    public ReservationTime convertToTime() {
        try {
            LocalTime startTime = LocalTime.parse(startAt);
            return new ReservationTime(startTime);
        } catch (DateTimeParseException e) {
            throw new IllegalStateException("시간형식이 잘못되었습니다");
        }
    }
}

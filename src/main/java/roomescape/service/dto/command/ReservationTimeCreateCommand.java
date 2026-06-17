package roomescape.service.dto.command;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

public record ReservationTimeCreateCommand(
        @DateTimeFormat(pattern = "hh:mm")
        @NotNull(message = "시작 시간은 비워둘 수 없습니다.")
        LocalTime startAt,

        @DateTimeFormat(pattern = "hh:mm")
        @NotNull(message = "종료 시간은 비워둘 수 없습니다.")
        LocalTime endAt
) {
}

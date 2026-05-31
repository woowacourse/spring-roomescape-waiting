package roomescape.reservationtime.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record TimeRequest(
        @NotNull(message = "시작 시간은 필수입니다.") LocalTime startAt,
        @NotNull(message = "종료 시간은 필수입니다.") LocalTime finishAt
) {
}

package roomescape.time.controller.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record ReservationTimeSaveDto(
        @NotNull(message = "startAt은 필수 입력값입니다.")
        LocalTime startAt
) {
}

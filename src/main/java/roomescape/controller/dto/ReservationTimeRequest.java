package roomescape.controller.dto;

import java.time.LocalTime;

import jakarta.validation.constraints.NotNull;

public record ReservationTimeRequest(
        @NotNull(message = "시간을 입력해주세요.")
        LocalTime startAt
) {
}

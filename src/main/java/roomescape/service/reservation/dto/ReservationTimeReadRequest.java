package roomescape.service.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReservationTimeReadRequest(
        @NotBlank(message = "날짜를 입력해주세요.") String date,
        @NotNull(message = "테마 ID를 입력해주세요.") long themeId
) {
}

package roomescape.service.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReservationRequest(
        @NotBlank(message = "날짜를 입력해주세요.") String date,
        @NotNull(message = "시간 ID를 입력해주세요.") long timeId,
        @NotNull(message = "테마 ID를 입력해주세요.") long themeId) {
}

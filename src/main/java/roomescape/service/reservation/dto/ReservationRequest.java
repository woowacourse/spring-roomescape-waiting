package roomescape.service.reservation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationRequest(
        LocalDate date,
        @NotNull(message = "시간 ID를 입력해주세요.") Long timeId,
        @NotNull(message = "테마 ID를 입력해주세요.") Long themeId) {
}

package roomescape.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationRequest(
        @NotBlank String name,
        @FutureOrPresent(message = "예약일은 현재 이후여야 합니다.") LocalDate date,
        @NotNull Long timeId,
        @NotNull Long themeId) {
}

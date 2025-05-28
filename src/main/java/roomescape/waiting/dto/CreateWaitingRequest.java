package roomescape.waiting.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateWaitingRequest(
        @NotNull(message = "날짜를 입력해주세요.") LocalDate date,
        long timeId,
        long themeId
) {
}

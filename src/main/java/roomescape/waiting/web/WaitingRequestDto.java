package roomescape.waiting.web;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record WaitingRequestDto(
        @NotNull LocalDate date,
        @NotNull Long timeId,
        @NotNull Long themeId,
        @NotNull Long storeId
) {
}

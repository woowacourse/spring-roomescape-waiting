package roomescape.waiting.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record WaitingRequestDto(
        @NotNull LocalDate date,
        @NotNull Long timeId,
        @NotNull Long themeId,
        @NotNull Long storeId
) {
}

package roomescape.waiting.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record WaitingRequest(
        @NotNull LocalDate date,
        @NotNull Long timeId,
        @NotNull Long themeId
) {
}

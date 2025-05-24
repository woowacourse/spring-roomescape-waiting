package roomescape.waiting.service.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateWaitingRequest(
        @NotNull
        LocalDate date,
        @NotNull
        Long themeId,
        @NotNull
        Long timeId
) {
}

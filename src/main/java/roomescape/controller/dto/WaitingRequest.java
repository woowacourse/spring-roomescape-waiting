package roomescape.controller.dto;

import jakarta.validation.constraints.NotNull;

public record WaitingRequest(
        @NotNull
        Long themeSlotId
) {
}

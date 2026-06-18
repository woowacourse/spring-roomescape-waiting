package roomescape.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ReservationRequest(
        @NotBlank
        String name,

        @NotNull
        Long themeSlotId
) { }

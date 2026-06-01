package roomescape.controller.dto;

import jakarta.validation.constraints.NotNull;

public record ReservationModifyRequest(
        @NotNull
        Long themeSlotId
) { }

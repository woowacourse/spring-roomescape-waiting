package roomescape.application.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationRequest(
        @NotNull LocalDate date,
        Long timeId,
        Long themeId
) {
}

package roomescape.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationRequest(
        @NotNull LocalDate date,
        long timeId,
        long themeId
) {
}

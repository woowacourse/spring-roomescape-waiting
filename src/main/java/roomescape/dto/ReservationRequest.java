package roomescape.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationRequest(
        @NotBlank
        String name,
        @NotNull
        LocalDate date,
        @NotNull
        Long timeId,
        @NotNull
        Long themeId
) {
}

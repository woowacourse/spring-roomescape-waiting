package roomescape.dto.reservation;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UpdateReservationRequest(
        @NotNull Long themeId,
        @NotNull LocalDate date,
        @NotNull Long timeId
) {
}

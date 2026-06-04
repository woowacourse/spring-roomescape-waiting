package roomescape.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateReservationRequest(
        @NotNull Long themeId,
        @NotNull LocalDate date,
        @NotNull Long timeId,
        @NotNull Long storeId
) {
}

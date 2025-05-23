package roomescape.service.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateReservationWaitRequest(
        @NotNull LocalDate date,
        @NotNull Long timeId,
        @NotNull Long themeId
) {
}

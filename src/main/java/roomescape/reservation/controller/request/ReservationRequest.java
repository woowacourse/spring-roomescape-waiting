package roomescape.reservation.controller.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationRequest(

        @NotNull
        LocalDate date,
        @NotNull
        Long themeId,
        @NotNull
        Long timeId
) {
}

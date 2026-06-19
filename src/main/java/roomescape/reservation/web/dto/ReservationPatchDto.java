package roomescape.reservation.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationPatchDto(
        @NotNull LocalDate date,
        @NotNull Long timeId
) {
}

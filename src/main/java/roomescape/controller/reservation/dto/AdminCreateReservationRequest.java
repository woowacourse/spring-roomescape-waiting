package roomescape.controller.reservation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AdminCreateReservationRequest(
        @NotNull
        Long memberId,

        @NotNull
        Long themeId,

        @NotNull
        LocalDate date,

        @NotNull
        Long timeId
) {
}

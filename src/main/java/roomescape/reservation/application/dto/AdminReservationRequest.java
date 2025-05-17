package roomescape.reservation.application.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AdminReservationRequest(
        @NotNull LocalDate date,
        @NotNull Long timeId,
        @NotNull Long themeId,
        @NotNull Long memberId
) {
}

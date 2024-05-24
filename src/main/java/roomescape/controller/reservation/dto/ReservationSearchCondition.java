package roomescape.controller.reservation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationSearchCondition(
        @NotNull
        Long themeId,

        @NotNull
        Long memberId,

        @NotNull
        LocalDate dateFrom,

        @NotNull
        LocalDate dateTo) {
}

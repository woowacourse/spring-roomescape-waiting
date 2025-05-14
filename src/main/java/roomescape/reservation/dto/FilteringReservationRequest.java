package roomescape.reservation.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record FilteringReservationRequest(
        @NotNull Long themeId,
        @NotNull Long memberId,
        @NotNull LocalDate dateFrom,
        @NotNull LocalDate dateTo
) {
}

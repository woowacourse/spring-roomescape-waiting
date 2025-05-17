package roomescape.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationReadFilteredRequest(
        @NotNull Long themeId,
        @NotNull Long memberId,
        @NotNull LocalDate dateFrom,
        @NotNull LocalDate dateTo
) {
}

package roomescape.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationSearchDto(
        @NotNull Long themeId,
        @NotNull Long memberId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {
}

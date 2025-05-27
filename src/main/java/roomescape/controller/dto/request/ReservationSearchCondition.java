package roomescape.controller.dto.request;

import java.time.LocalDate;

public record ReservationSearchCondition(
        Long memberId,
        Long themeId,
        LocalDate dateFrom,
        LocalDate dateTo
) {
}

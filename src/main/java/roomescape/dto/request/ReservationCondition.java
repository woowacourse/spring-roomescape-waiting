package roomescape.dto.request;

import java.time.LocalDate;

public record ReservationCondition(
        Long themeId,
        Long memberId,
        LocalDate dateFrom,
        LocalDate dateTo
) {
}

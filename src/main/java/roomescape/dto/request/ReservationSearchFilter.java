package roomescape.dto.request;

import java.time.LocalDate;

public record ReservationSearchFilter(
        Long themeId,
        Long memberId,
        LocalDate startDate,
        LocalDate endDate
) {
}

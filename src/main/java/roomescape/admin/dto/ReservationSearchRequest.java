package roomescape.admin.dto;

import java.time.LocalDate;

public record ReservationSearchRequest(
        Long themeId,
        Long memberId,
        LocalDate dateFrom,
        LocalDate dateTo
) {
}

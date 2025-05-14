package roomescape.dto.request;

import java.time.LocalDate;

public record ReservationSearchDto(
        Long themeId,
        Long memberId,
        LocalDate startDate,
        LocalDate endDate
) {
}

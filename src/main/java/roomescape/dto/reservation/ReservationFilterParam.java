package roomescape.dto.reservation;

import java.time.LocalDate;

public record ReservationFilterParam(
        Long themeId,
        Long memberId,
        LocalDate dateFrom,
        LocalDate dateTo
) {
}

package roomescape.service.reservation.dto;

import java.time.LocalDate;

public record ReservationFindRequest(
        Long memberId,
        Long themeId,
        LocalDate dateFrom,
        LocalDate dateTo
) {
}

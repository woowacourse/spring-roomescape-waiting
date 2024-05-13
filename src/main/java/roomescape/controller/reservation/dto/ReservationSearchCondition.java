package roomescape.controller.reservation.dto;

import java.time.LocalDate;

public record ReservationSearchCondition(
        Long themeId,
        Long memberId,
        LocalDate dateFrom,
        LocalDate dateTo) {
}

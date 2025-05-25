package roomescape.application.reservation.query.dto;

import java.time.LocalDate;

public record ReservationSearchCondition(
        Long themeId,
        Long memberId,
        LocalDate from,
        LocalDate to
) {
}

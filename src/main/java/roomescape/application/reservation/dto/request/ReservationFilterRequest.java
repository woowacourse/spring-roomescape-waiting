package roomescape.application.reservation.dto.request;

import java.time.LocalDate;

public record ReservationFilterRequest(
        Long memberId,
        Long themeId,
        LocalDate startDate,
        LocalDate endDate) {
}

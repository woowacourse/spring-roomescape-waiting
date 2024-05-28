package roomescape.reservation.dto.request;

import java.time.LocalDate;

public record ReservationSearchRequest(
        Long themeId,
        Long memberId,
        LocalDate dateFrom,
        LocalDate dateTo,
        Boolean waiting
) {
}

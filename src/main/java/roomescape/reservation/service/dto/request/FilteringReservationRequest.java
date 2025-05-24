package roomescape.reservation.service.dto.request;

import java.time.LocalDate;

public record FilteringReservationRequest(
        Long themeId,
        Long memberId,
        LocalDate dateFrom,
        LocalDate dateTo
) {
}

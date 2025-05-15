package roomescape.reservation.dto;

import java.time.LocalDate;

public record FilteringReservationRequest(
        Long themeId,
        Long memberId,
        LocalDate dateFrom,
        LocalDate dateTo
) {
}

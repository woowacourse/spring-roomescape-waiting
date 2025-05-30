package roomescape.reservation.dto.request;

import java.time.LocalDate;

public record ReservationSearchConditionRequest(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
}

package roomescape.reservation.service.dto;

import java.time.LocalDate;

public record ReservationSearchCondition(Long memberId, Long themeId, LocalDate fromDate, LocalDate toDate) {
}

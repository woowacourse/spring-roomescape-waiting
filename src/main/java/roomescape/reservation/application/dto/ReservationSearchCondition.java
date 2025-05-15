package roomescape.reservation.application.dto;

import java.time.LocalDate;

public record ReservationSearchCondition(Long memberId, Long themeId, LocalDate fromDate, LocalDate toDate) {
}

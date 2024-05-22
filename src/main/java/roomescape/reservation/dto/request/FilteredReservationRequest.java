package roomescape.reservation.dto.request;

import java.time.LocalDate;

public record FilteredReservationRequest(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
}

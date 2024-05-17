package roomescape.service.request;

import java.time.LocalDate;

public record AdminSearchedReservationAppRequest(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo) {
}

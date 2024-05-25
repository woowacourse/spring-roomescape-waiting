package roomescape.service.request;

import java.time.LocalDate;

public record AdminSearchedReservationDto(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo) {
}

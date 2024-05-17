package roomescape.dto.request;

import java.time.LocalDate;

public record ReservationFilterRequest(Long memberId,
                                       Long themeId,
                                       LocalDate dateFrom,
                                       LocalDate dateTo) {
}

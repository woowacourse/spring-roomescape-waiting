package roomescape.reservation.dto;

import java.time.LocalDate;

public record ReservationSearchRequestParameter(LocalDate dateFrom,
                                                LocalDate dateTo,
                                                Long memberId,
                                                Long themeId
) {
}

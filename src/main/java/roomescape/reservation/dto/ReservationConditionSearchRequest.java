package roomescape.reservation.dto;

import java.time.LocalDate;

public record ReservationConditionSearchRequest(
        long memberId,
        long themeId,
        LocalDate dateFrom,
        LocalDate dateTo
) {

}

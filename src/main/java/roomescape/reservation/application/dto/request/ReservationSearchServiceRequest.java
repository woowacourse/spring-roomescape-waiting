package roomescape.reservation.application.dto.request;

import java.time.LocalDate;

public record ReservationSearchServiceRequest(
        Long themeId,
        Long memberId,
        LocalDate dateFrom,
        LocalDate dateTo
) {

}

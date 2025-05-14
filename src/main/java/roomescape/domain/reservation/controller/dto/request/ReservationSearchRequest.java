package roomescape.domain.reservation.controller.dto.request;

import java.time.LocalDate;
import roomescape.domain.reservation.application.dto.request.ReservationSearchServiceRequest;

public record ReservationSearchRequest(
        Long themeId,
        Long memberId,
        LocalDate dateFrom,
        LocalDate dateTo
) {
    public ReservationSearchServiceRequest toServiceRequest() {
        return new ReservationSearchServiceRequest(themeId, memberId, dateFrom, dateTo);
    }
}

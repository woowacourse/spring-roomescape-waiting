package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import roomescape.reservation.service.dto.ReservationSearchCondition;

public record ReservationSearchConditionRequest(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo) {

    public ReservationSearchCondition toCondition() {
        return new ReservationSearchCondition(memberId, themeId, dateFrom, dateTo);
    }
}

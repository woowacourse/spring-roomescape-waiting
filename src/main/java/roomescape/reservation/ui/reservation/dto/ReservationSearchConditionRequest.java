package roomescape.reservation.ui.reservation.dto;

import java.time.LocalDate;
import roomescape.reservation.application.reservation.dto.ReservationSearchCondition;

public record ReservationSearchConditionRequest(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo) {

    public ReservationSearchCondition toCondition() {
        return new ReservationSearchCondition(memberId, themeId, dateFrom, dateTo);
    }
}

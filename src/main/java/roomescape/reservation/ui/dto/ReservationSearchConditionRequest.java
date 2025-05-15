package roomescape.reservation.ui.dto;

import java.time.LocalDate;
import roomescape.reservation.application.dto.ReservationSearchCondition;

public record ReservationSearchConditionRequest(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo) {

    public ReservationSearchCondition toCondition() {
        return new ReservationSearchCondition(memberId, themeId, dateFrom, dateTo);
    }
}

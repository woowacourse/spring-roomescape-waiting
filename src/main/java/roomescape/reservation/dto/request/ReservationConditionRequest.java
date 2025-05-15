package roomescape.reservation.dto.request;

import java.time.LocalDate;

public record ReservationConditionRequest(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo) {

    public boolean isEmpty() {
        return memberId == null && themeId == null && dateFrom == null && dateTo == null;
    }
}

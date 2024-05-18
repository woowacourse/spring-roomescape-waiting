package roomescape.service.dto.request;

import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record ReservationConditionRequest(
        @Positive
        Long themeId,

        @Positive
        Long memberId,

        LocalDate dateFrom,

        LocalDate dateTo
) {
    public boolean hasNoneCondition() {
        return themeId == null && memberId == null && dateFrom == null && dateTo == null;
    }
}

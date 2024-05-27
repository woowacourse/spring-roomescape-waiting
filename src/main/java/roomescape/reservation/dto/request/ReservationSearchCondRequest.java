package roomescape.reservation.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.common.validator.ValidDateRange;

@ValidDateRange
@NotNull
public record ReservationSearchCondRequest(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
}


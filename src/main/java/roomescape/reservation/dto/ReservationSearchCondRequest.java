package roomescape.reservation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.validator.ValidDateRange;

@ValidDateRange
@NotNull
public record ReservationSearchCondRequest(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
}


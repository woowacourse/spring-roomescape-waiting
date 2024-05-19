package roomescape.reservation.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.exception.ValidDate;

@ValidDate
@NotNull
public record ReservationSearchRequest(
        Long themeId,
        Long memberId,
        LocalDate dateFrom,
        LocalDate dateTo
) {
}


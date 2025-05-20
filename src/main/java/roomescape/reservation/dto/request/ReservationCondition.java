package roomescape.reservation.dto.request;

import jakarta.annotation.Nullable;
import java.time.LocalDate;

public record ReservationCondition(
        @Nullable Long themeId,
        @Nullable Long memberId,
        @Nullable LocalDate dateFrom,
        @Nullable LocalDate dateTo
) {
}

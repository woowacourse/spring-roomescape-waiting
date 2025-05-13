package roomescape.domain.repository;

import jakarta.annotation.Nullable;
import java.time.LocalDate;

public record ReservationSearchFilter(
    @Nullable Long themeId,
    @Nullable Long userId,
    @Nullable LocalDate dateFrom,
    @Nullable LocalDate dateTo
) {

}

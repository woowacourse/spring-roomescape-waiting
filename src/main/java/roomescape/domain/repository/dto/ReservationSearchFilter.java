package roomescape.domain.repository.dto;

import java.time.LocalDate;
import org.springframework.lang.Nullable;

public record ReservationSearchFilter(
        @Nullable
        Long themeId,
        @Nullable
        Long memberId,
        @Nullable
        LocalDate dateFrom,
        @Nullable
        LocalDate dateTo
) {
    public boolean isEmpty() {
        return themeId == null && memberId == null && dateFrom == null && dateTo == null;
    }
}

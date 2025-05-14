package roomescape.dto.request;

import java.time.LocalDate;
import java.util.Optional;

public record ReservationCondition(
        Optional<Long> themeId,
        Optional<Long> memberId,
        Optional<LocalDate> dateFrom,
        Optional<LocalDate> dateTo
) {
}

package roomescape.repository.result;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservationWaitingOrderResult(
        Long id,
        LocalDate date,
        Long timeId,
        Long themeId,
        LocalDateTime createdAt
) {
}

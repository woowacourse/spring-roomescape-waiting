package roomescape.dto.reservation;

import java.time.LocalDate;

public record ReservationExistenceCheck(
        LocalDate date,
        Long timeId,
        Long themeId
) {
}

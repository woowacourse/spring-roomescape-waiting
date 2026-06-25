package roomescape.service.dto;

import java.time.LocalDate;

public record ReservationAvailableEvent(
        LocalDate date,
        Long timeId,
        Long themeId
) {
}

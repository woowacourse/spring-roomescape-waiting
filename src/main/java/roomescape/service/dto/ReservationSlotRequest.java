package roomescape.service.dto;

import java.time.LocalDate;

public record ReservationSlotRequest(
        LocalDate date,
        Long timeId,
        Long themeId
) {
}

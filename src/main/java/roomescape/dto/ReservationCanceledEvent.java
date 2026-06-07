package roomescape.dto;

import java.time.LocalDate;

public record ReservationCanceledEvent(
        LocalDate date,
        Long timeId,
        Long themeId
) {
}

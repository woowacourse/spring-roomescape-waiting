package roomescape.service.dto;

import java.time.LocalDate;

public record ReservationCreateCommand(
        String reserverName,
        LocalDate date,
        Long timeId,
        Long themeId
) {
}

package roomescape.service.command;

import java.time.LocalDate;

public record ReservationCommand(
        String name,
        LocalDate date,
        Long themeId,
        Long timeId
) {
}

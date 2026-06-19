package roomescape.application.service.command;

import java.time.LocalDate;
import roomescape.domain.ReservationStatus;

public record ReservationPendingCommand(
        String name,
        LocalDate date,
        Long themeId,
        Long timeId,
        ReservationStatus status
) {
}

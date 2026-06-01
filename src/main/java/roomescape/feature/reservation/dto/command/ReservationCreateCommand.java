package roomescape.feature.reservation.dto.command;

import java.time.LocalDate;
import roomescape.feature.reservation.domain.ReserverName;

public record ReservationCreateCommand(
    ReserverName name,
    LocalDate date,
    Long timeId,
    Long themeId
) {
}

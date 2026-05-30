package roomescape.feature.reservation.dto.command;

import java.time.LocalDate;
import roomescape.feature.reservation.domain.ReserverName;

public record ReservationUpdateCommand(
    ReserverName name,
    LocalDate date,
    Long timeId,
    Long themeId
) {
}

package roomescape.domain.reservation.dto.command;

import java.time.LocalDate;
import roomescape.domain.reservation.vo.ReserverName;

public record ReservationUpdateCommand(
    ReserverName name,
    LocalDate date,
    Long timeId,
    Long themeId
) {
}

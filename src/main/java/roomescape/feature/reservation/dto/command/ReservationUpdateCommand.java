package roomescape.feature.reservation.dto.command;

import java.time.LocalDate;
import roomescape.feature.reservation.domain.ReserverName;
import roomescape.global.error.exception.GeneralException;
import roomescape.global.error.type.GeneralErrorType;

public record ReservationUpdateCommand(
    ReserverName name,
    LocalDate date,
    Long timeId,
    Long themeId
) {
    public ReservationUpdateCommand {
        if (name == null || date == null || timeId == null || themeId == null) {
            throw new GeneralException(GeneralErrorType.ILLEGAL_STATE);
        }
    }
}

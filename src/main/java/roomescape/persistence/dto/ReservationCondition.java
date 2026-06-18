package roomescape.persistence.dto;

import java.time.LocalDate;
import roomescape.application.service.command.ReservationCommand;

public record ReservationCondition(
       LocalDate date,
       long themeId,
       long timeId
) {

    public static ReservationCondition from(ReservationCommand command) {
        return new ReservationCondition(command.date(), command.themeId(), command.timeId());
    }
}

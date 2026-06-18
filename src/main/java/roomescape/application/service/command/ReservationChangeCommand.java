package roomescape.application.service.command;

import java.time.LocalDate;
import roomescape.persistence.dto.ReservationCondition;

public record ReservationChangeCommand(
        LocalDate date,
        Long timeId
) {

    public ReservationCondition toCondition(long themeId) {
        return new ReservationCondition(date, themeId, timeId);
    }
}

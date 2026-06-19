package roomescape.service.command;

import java.time.LocalDate;
import roomescape.repository.dto.ReservationCondition;

public record ReservationCommand(
        String name,
        LocalDate date,
        Long themeId,
        Long timeId,
        Long amount
) {

    public ReservationCondition toCondition() {
        return new ReservationCondition(date, themeId, timeId);
    }

    public ReservationCondition toCondition(long themeId) {
        return new ReservationCondition(date, themeId, timeId);
    }
}

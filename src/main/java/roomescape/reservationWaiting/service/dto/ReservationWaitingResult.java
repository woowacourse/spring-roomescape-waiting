package roomescape.reservationWaiting.service.dto;

import java.time.LocalDate;
import roomescape.reservationWaiting.domain.ReservationWaiting;
import roomescape.theme.service.dto.ThemeResult;
import roomescape.time.service.dto.ReservationTimeResult;

public record ReservationWaitingResult(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResult time,
        ThemeResult theme
) {
    public static ReservationWaitingResult from(ReservationWaiting waiting) {
        return new ReservationWaitingResult(
                waiting.id(),
                waiting.name(),
                waiting.date(),
                ReservationTimeResult.from(waiting.time()),
                ThemeResult.from(waiting.theme())
        );
    }
}

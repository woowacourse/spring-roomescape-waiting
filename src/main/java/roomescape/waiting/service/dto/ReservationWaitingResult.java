package roomescape.waiting.service.dto;

import java.time.LocalDate;
import roomescape.theme.service.dto.ThemeResult;
import roomescape.time.service.dto.ReservationTimeResult;
import roomescape.waiting.domain.ReservationWaiting;

public record ReservationWaitingResult(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResult time,
        ThemeResult theme
) {
    public static ReservationWaitingResult from(ReservationWaiting waiting) {
        return new ReservationWaitingResult(
                waiting.getId(),
                waiting.getName(),
                waiting.getDate(),
                ReservationTimeResult.from(waiting.getTime()),
                ThemeResult.from(waiting.getTheme())
        );
    }
}

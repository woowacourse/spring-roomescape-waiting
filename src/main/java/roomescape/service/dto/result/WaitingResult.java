package roomescape.service.dto.result;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.reservation.Waiting;

public record WaitingResult(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResult timeResult,
        ThemeResult themeResult,
        LocalDateTime createdAt
) {

    public static WaitingResult from(Waiting waiting) {
        return new WaitingResult(
                waiting.getId(),
                waiting.getName().value(),
                waiting.getDate(),
                ReservationTimeResult.from(waiting.getTime()),
                ThemeResult.from(waiting.getTheme()),
                waiting.getCreatedAt()
        );
    }
}

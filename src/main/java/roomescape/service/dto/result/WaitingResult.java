package roomescape.service.dto.result;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.waiting.Waiting;

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
                waiting.getSlot().date(),
                ReservationTimeResult.from(waiting.getSlot().time()),
                ThemeResult.from(waiting.getSlot().theme()),
                waiting.getCreatedAt()
        );
    }
}

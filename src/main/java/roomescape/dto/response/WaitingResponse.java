package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import roomescape.domain.reservation.Waiting;

public record WaitingResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse timeResponse,
        ThemeResponse themeResponse,
        LocalDateTime createdAt
) {

    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(
                waiting.getId(),
                waiting.getName().value(),
                waiting.getDate(),
                ReservationTimeResponse.from(waiting.getTime()),
                ThemeResponse.from(waiting.getTheme()),
                waiting.getCreatedAt()
        );
    }
}

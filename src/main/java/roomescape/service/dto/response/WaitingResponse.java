package roomescape.service.dto.response;

import java.time.LocalDate;
import roomescape.domain.Waiting;

public record WaitingResponse(Long id, LocalDate date, String name, ReservationTimeResponse time, ThemeResponse theme) {
    public WaitingResponse(Waiting waiting) {
        this(waiting.getId(),
                waiting.getDate(),
                waiting.getMember().getName(),
                new ReservationTimeResponse(waiting.getTime()),
                new ThemeResponse(waiting.getTheme()));
    }
}

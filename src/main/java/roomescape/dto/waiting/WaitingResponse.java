package roomescape.dto.waiting;

import java.time.LocalDate;
import roomescape.domain.waiting.Waiting;
import roomescape.dto.theme.ThemeResponse;
import roomescape.dto.time.ReservationTimeResponse;

public record WaitingResponse(
        long id,
        LocalDate date,
        ThemeResponse theme,
        ReservationTimeResponse time) {
    public static WaitingResponse from(Waiting waiting) {
        return new WaitingResponse(waiting.getId(), waiting.getDate(), ThemeResponse.from(waiting.getTheme()),
                ReservationTimeResponse.from(waiting.getTime()));
    }
}

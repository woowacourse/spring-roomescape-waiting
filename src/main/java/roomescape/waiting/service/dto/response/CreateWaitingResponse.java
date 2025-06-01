package roomescape.waiting.service.dto.response;

import roomescape.reservation.service.dto.response.ReservationTimeResponse;
import roomescape.theme.service.dto.response.ThemeResponse;
import roomescape.waiting.domain.Waiting;

import java.time.LocalDate;

public record CreateWaitingResponse(
        Long id,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {
    public static CreateWaitingResponse from(Waiting waiting) {
        return new CreateWaitingResponse(
                waiting.getId(),
                waiting.getDate(),
                ReservationTimeResponse.from(waiting.getTime()),
                ThemeResponse.from(waiting.getTheme())
        );
    }
}

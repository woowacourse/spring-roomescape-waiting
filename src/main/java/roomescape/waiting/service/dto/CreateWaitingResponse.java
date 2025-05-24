package roomescape.waiting.service.dto;

import roomescape.reservation.dto.response.ReservationTimeResponse;
import roomescape.theme.dto.response.ThemeResponse;
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

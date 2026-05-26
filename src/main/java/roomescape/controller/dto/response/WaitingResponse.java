package roomescape.controller.dto.response;

import roomescape.domain.Waiting;
import roomescape.service.dto.WaitingResult;

import java.time.LocalDate;

public record WaitingResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        Long order
) {

    public static WaitingResponse from(WaitingResult waitingResult) {
        Waiting waiting = waitingResult.waiting();
        return new WaitingResponse(
                waiting.getId(),
                waiting.getName(),
                waiting.getDate(),
                ReservationTimeResponse.from(waiting.getTime()),
                ThemeResponse.from(waiting.getTheme()),
                waitingResult.order()
                );
    }
}

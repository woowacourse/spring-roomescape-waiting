package roomescape.controller.dto;

import roomescape.service.result.WaitingResult;

import java.time.LocalDate;

public record ReservationWaitingResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationWaitingTimeResponse time,
        ReservationWaitingThemeResponse theme,
        Long turn
) {

    public static ReservationWaitingResponse from(WaitingResult waitingResult) {
        return new ReservationWaitingResponse(
                waitingResult.id(),
                waitingResult.name(),
                waitingResult.date(),
                ReservationWaitingTimeResponse.from(waitingResult.time()),
                ReservationWaitingThemeResponse.from(waitingResult.theme()),
                waitingResult.turn()
        );
    }
}

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

    public static ReservationWaitingResponse from(WaitingResult waitingWithTurn) {
        return new ReservationWaitingResponse(
                waitingWithTurn.id(),
                waitingWithTurn.name(),
                waitingWithTurn.date(),
                ReservationWaitingTimeResponse.from(waitingWithTurn.time()),
                ReservationWaitingThemeResponse.from(waitingWithTurn.theme()),
                waitingWithTurn.turn());
    }
}

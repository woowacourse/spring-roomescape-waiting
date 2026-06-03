package roomescape.controller.dto;

import roomescape.domain.ReservationWaiting;
import roomescape.domain.WaitingWithTurn;

import java.time.LocalDate;

public record ReservationWaitingResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ReservationThemeResponse theme,
        Long turn
) {

    public static ReservationWaitingResponse from(WaitingWithTurn waitingWithTurn) {
        ReservationWaiting waiting = waitingWithTurn.waiting();
        return new ReservationWaitingResponse(
                waiting.getId(),
                waiting.getName(),
                waiting.getDate(),
                ReservationTimeResponse.from(waiting.getTime()),
                ReservationThemeResponse.from(waiting.getTheme()),
                waitingWithTurn.turn()
        );
    }
}

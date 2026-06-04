package roomescape.controller.dto.response;

import roomescape.domain.ReservationSlot;
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
        ReservationSlot slot = waiting.getSlot();
        return new ReservationWaitingResponse(
                waiting.getId(),
                waiting.getName(),
                slot.getDate(),
                ReservationTimeResponse.from(slot.getTime()),
                ReservationThemeResponse.from(slot.getTheme()),
                waitingWithTurn.turn()
        );
    }
}

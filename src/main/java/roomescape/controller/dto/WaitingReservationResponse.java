package roomescape.controller.dto;

import java.time.LocalDate;
import roomescape.domain.WaitingReservation;

public record WaitingReservationResponse(
        long id,
        String name,
        LocalDate date,
        TimeResponse time,
        ThemeResponse theme,
        String status,
        int waitingOrder
) {
    public static WaitingReservationResponse from(WaitingReservation waitingReservation) {
        return new WaitingReservationResponse(
                waitingReservation.id(),
                waitingReservation.name(),
                waitingReservation.date(),
                TimeResponse.from(waitingReservation.time()),
                ThemeResponse.from(waitingReservation.theme()),
                waitingReservation.status(),
                waitingReservation.waitingOrder()
        );
    }
}

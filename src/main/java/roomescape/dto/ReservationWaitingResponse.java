package roomescape.dto;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.WaitingWithOrder;

import java.time.LocalDate;

public record ReservationWaitingResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        int order
) {
    public static ReservationWaitingResponse from(WaitingWithOrder waitingWithOrder) {
        ReservationWaiting waiting = waitingWithOrder.getWaiting();
        Reservation reservation = waiting.getReservation();
        return new ReservationWaitingResponse(
                waiting.getId(),
                waiting.getName(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                waitingWithOrder.getOrder()
        );
    }
}

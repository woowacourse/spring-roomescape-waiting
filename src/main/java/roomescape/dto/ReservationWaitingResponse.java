package roomescape.dto;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.projection.ReservationWaitingWithOrder;

public record ReservationWaitingResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        int order
) {
    public static ReservationWaitingResponse from(ReservationWaitingWithOrder reservationWaiting) {
        Reservation reservation = reservationWaiting.reservation();
        return new ReservationWaitingResponse(
                reservationWaiting.id(),
                reservationWaiting.name(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                reservationWaiting.order()
        );
    }
}

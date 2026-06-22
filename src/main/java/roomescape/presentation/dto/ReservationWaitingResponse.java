package roomescape.presentation.dto;

import java.time.LocalDate;
import roomescape.domain.projection.ReservationWaitingWithOrder;

public record ReservationWaitingResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        int order
) {
    public static ReservationWaitingResponse from(ReservationWaitingWithOrder reservationWaiting) {
        return new ReservationWaitingResponse(
                reservationWaiting.id(),
                reservationWaiting.name(),
                reservationWaiting.date(),
                ReservationTimeResponse.from(reservationWaiting.time()),
                ThemeResponse.from(reservationWaiting.theme()),
                reservationWaiting.order()
        );
    }
}

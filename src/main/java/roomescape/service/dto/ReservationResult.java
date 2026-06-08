package roomescape.service.dto;

import java.time.LocalDate;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationWithWaitingOrder;
import roomescape.domain.WaitingOrder;

public record ReservationResult(
        Long id,
        String reserverName,
        LocalDate date,
        ReservationTimeResult time,
        ThemeResult theme,
        long waitingOrder,
        ReservationStatus status
) {
    public static ReservationResult from(ReservationWithWaitingOrder reservation) {
        WaitingOrder waitingOrder = reservation.waitingOrder();
        return new ReservationResult(
                reservation.id(),
                reservation.reserverName(),
                reservation.date(),
                ReservationTimeResult.from(reservation.time()),
                ThemeResult.from(reservation.theme()),
                waitingOrder.value(),
                reservation.status()
        );
    }
}

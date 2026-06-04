package roomescape.service.dto;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public record ReservationWithWaitingOrder(
        Long id,
        String name,
        LocalDate date,
        ReservationTime time,
        Theme theme,
        Long waitingOrder
) {
    public static ReservationWithWaitingOrder from(
            Reservation reservation,
            Long waitingOrder
    ) {
        return new ReservationWithWaitingOrder(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                waitingOrder
        );
    }
}

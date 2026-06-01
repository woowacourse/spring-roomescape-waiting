package roomescape.service.dto;

import java.time.LocalDate;

public record ReservationResult(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResult time,
        ThemeResult theme,
        Long waitingOrder
) {
    public static ReservationResult from(ReservationWithWaitingOrder reservation) {
        return new ReservationResult(
                reservation.id(),
                reservation.name(),
                reservation.date(),
                ReservationTimeResult.from(reservation.time()),
                ThemeResult.from(reservation.theme()),
                reservation.waitingOrder()
        );
    }
}

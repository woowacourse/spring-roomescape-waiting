package roomescape.reservation.service.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.service.dto.ThemeResult;
import roomescape.time.service.dto.ReservationTimeResult;

public record ReservationResult(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResult time,
        ThemeResult theme,
        String orderId
) {
    public static ReservationResult from(Reservation reservation) {
        return from(reservation, null);
    }

    public static ReservationResult from(Reservation reservation, String orderId) {
        return new ReservationResult(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ReservationTimeResult.from(reservation.getTime()),
                ThemeResult.from(reservation.getTheme()),
                orderId
        );
    }
}

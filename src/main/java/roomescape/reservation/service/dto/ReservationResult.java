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
        ThemeResult theme
) {
    public static ReservationResult from(Reservation reservation) {
        return new ReservationResult(
                reservation.id(),
                reservation.name(),
                reservation.date(),
                ReservationTimeResult.from(reservation.time()),
                ThemeResult.from(reservation.theme())
        );
    }
}

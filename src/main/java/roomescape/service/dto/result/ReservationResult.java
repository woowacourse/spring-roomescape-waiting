package roomescape.service.dto.result;

import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;

public record ReservationResult(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResult timeResult,
        ThemeResult themeResult
) {

    public static ReservationResult from(Reservation reservation) {
        return new ReservationResult(
                reservation.getId(),
                reservation.getName().value(),
                reservation.getDate(),
                ReservationTimeResult.from(reservation.getTime()),
                ThemeResult.from(reservation.getTheme())
        );
    }
}

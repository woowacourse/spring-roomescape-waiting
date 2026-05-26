package roomescape.service.result;

import java.time.LocalDate;
import roomescape.domain.Reservation;

public record ReservationResult(long id, String name, LocalDate date, ThemeResult theme, ReservationTimeResult time) {

    public static ReservationResult from(Reservation reservation) {
        return new ReservationResult(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ThemeResult.from(reservation.getTheme()),
                ReservationTimeResult.from(reservation.getTime())
        );
    }
}

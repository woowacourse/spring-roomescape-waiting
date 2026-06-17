package roomescape.service.dto.result;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.service.dto.ReservationStatus;

import java.time.LocalDate;

public record ReservationResult (
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResult time,
        ThemeResult theme,
        ReservationStatus status
) {

    public static ReservationResult from(final Reservation reservation) {
        final ReservationTime reservationTime = reservation.getTime();
        final Theme theme = reservation.getTheme();

        return new ReservationResult(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ReservationTimeResult.from(reservationTime),
                ThemeResult.from(theme),
                ReservationStatus.RESERVATION
        );
    }
}

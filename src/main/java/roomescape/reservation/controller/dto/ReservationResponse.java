package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import roomescape.reservation.Reservation;
import roomescape.reservationtime.controller.dto.ReservationTimeResponse;
import roomescape.theme.controller.dto.ThemeResponse;

public record ReservationResponse(
        Long id,
        String name,
        LocalDate date,
        ThemeResponse theme,
        ReservationTimeResponse time
) {

    public static ReservationResponse from(final Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ThemeResponse.from(reservation.getTheme()),
                ReservationTimeResponse.from(reservation.getTime())
        );
    }
}

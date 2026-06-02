package roomescape.controller.reservation.dto;

import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;
import roomescape.controller.reservationtime.dto.ReservationTimeResponse;
import roomescape.controller.theme.dto.ThemeResponse;

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

package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.controller.dto.ThemeResponse;
import roomescape.time.controller.dto.ReservationTimeResponse;

public record ReservationResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getReservationTime()),
                ThemeResponse.from(reservation.getTheme())
        );
    }
}

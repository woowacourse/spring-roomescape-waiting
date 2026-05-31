package roomescape.reservation.controller.dto;

import roomescape.reservation.domain.Reservation;
import roomescape.theme.controller.dto.ThemeResponse;

public record ReservationResponse(
        Long id,
        String name,
        ReservationTimeResponse time,
        ThemeResponse theme
) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme())
        );
    }
}

package roomescape.reservation.controller.dto.response;

import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.controller.dto.response.ReservationTimeResponse;
import roomescape.theme.controller.dto.response.ThemeResponse;

import java.time.LocalDate;

public record ReservationResponse(
        Long id,
        String name,
        String email,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {

    public static ReservationResponse from(final Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getCustomerName(),
                reservation.getCustomerEmail(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme())
        );
    }
}

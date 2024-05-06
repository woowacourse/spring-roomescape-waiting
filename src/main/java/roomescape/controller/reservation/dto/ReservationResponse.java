package roomescape.controller.reservation.dto;

import roomescape.controller.theme.dto.ReservationThemeResponse;
import roomescape.controller.time.dto.AvailabilityTimeResponse;
import roomescape.domain.Reservation;

import java.time.format.DateTimeFormatter;

public record ReservationResponse(Long id, String name, String date, AvailabilityTimeResponse time,
                                  ReservationThemeResponse theme) {

    public static ReservationResponse from(final Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE),
                AvailabilityTimeResponse.from(reservation.getTime(), false),
                ReservationThemeResponse.from(reservation.getTheme())
        );
    }
}

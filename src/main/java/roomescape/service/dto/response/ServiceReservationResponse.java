package roomescape.service.dto.response;

import java.time.LocalDate;
import roomescape.domain.Reservation;

public record ServiceReservationResponse(
        Long id,
        String name,
        LocalDate date,
        ServiceReservationTimeResponse time,
        ServiceThemeResponse theme
) {
    public static ServiceReservationResponse from(Reservation reservation) {
        return new ServiceReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ServiceReservationTimeResponse.from(reservation.getTime()),
                ServiceThemeResponse.from(reservation.getTheme()));
    }
}

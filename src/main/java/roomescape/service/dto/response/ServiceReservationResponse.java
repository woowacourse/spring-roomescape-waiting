package roomescape.service.dto.response;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;

public record ServiceReservationResponse(
        Long id,
        String name,
        LocalDate date,
        ServiceReservationTimeResponse time,
        ServiceThemeResponse theme,
        ReservationStatus status
) implements ServiceReceptionResponse {

    public static ServiceReservationResponse from(Reservation reservation) {
        return new ServiceReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ServiceReservationTimeResponse.from(reservation.getTime()),
                ServiceThemeResponse.from(reservation.getTheme()),
                ReservationStatus.CONFIRMED
        );
    }
}

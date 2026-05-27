package roomescape.service.dto.response;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.domain.Wait;

public record ServiceReceptionResponse(
        Long id,
        Long order,
        String name,
        LocalDate reservationDate,
        ServiceReservationTimeResponse time,
        ServiceThemeResponse theme,
        String status
) {
    public static ServiceReceptionResponse of(Reservation reservation, Long order, String status) {
        return new ServiceReceptionResponse(
                reservation.getId(),
                order,
                reservation.getName(),
                reservation.getDate(),
                ServiceReservationTimeResponse.from(reservation.getTime()),
                ServiceThemeResponse.from(reservation.getTheme()),
                status
        );
    }

    public static ServiceReceptionResponse of(Wait wait, Long order, String status) {
        return new ServiceReceptionResponse(
                wait.getId(),
                order,
                wait.getName(),
                wait.getReservationDate(),
                ServiceReservationTimeResponse.from(wait.getTime()),
                ServiceThemeResponse.from(wait.getTheme()),
                status
        );
    }
}

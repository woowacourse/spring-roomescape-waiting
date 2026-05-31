package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

public record ReservationResponse(
        Long id,
        String name,
        LocalDate date,
        TimeResponse time,
        ThemeSimpleResponse theme,
        ReservationStatus status
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                TimeResponse.from(reservation.getTime()),
                ThemeSimpleResponse.from(reservation.getTheme()),
                reservation.getStatus()
        );
    }
}
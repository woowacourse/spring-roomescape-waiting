package roomescape.domain.reservation.dto;

import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;

public record ReservationResponse(
    Long id,
    String name,
    LocalDate date,
    Long timeId,
    Long themeId
) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
            reservation.getId(),
            reservation.getName(),
            reservation.getDate(),
            reservation.getTime().getId(),
            reservation.getTheme().getId()
        );
    }
}

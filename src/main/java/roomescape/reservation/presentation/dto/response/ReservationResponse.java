package roomescape.reservation.presentation.dto.response;

import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record ReservationResponse(
        Long id,
        String name,
        String date,
        LocalTime time,
        String theme
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate().toString(),
                reservation.getTime().getStartAt(),
                reservation.getTheme().getName()
        );
    }
}

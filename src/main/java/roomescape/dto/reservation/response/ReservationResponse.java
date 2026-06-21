package roomescape.dto.reservation.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;

public record ReservationResponse(
        Long id,
        String name,
        String themeName,
        LocalDate date,
        LocalTime time,
        ReservationStatus status
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser().getName(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getStatus()
        );
    }
}

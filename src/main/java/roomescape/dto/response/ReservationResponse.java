package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.Reservation;

public record ReservationResponse(
        Long id,
        String name,
        String themeName,
        LocalDate date,
        LocalTime time
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getUser().getName(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt()
        );
    }
}

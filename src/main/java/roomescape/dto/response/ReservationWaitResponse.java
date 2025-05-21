package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.entity.Reservation;

public record ReservationWaitResponse(
        Long id,
        String name,
        LocalTime time,
        LocalDate date,
        String themeName
) {
    public static ReservationWaitResponse from(Reservation reservation) {

        return new ReservationWaitResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getStartAt(),
                reservation.getDate(),
                reservation.getThemeName()
        );
    }
}

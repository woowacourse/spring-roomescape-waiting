package roomescape.reservation.application.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.model.entity.Reservation;

public record ReservationServiceResponse(
        Long id,
        String name,
        LocalDate date,
        LocalTime startAt,
        String themeName
) {

    public static ReservationServiceResponse from(Reservation reservation) {
        return new ReservationServiceResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getTheme().getName()
        );
    }
}

package roomescape.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.entity.Reservation;

public record ReservationResponse(Long id, String name, LocalTime time, LocalDate date, String themeName) {
    public static ReservationResponse from(Reservation reservation) {

        return new ReservationResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getStartAt(),
                reservation.getDate(),
                reservation.getThemeName()
        );
    }
}

package roomescape.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.entity.Reservation;

public record ReservationResponse(
        Long id,
        LocalDate date,
        LocalTime startAt,
        String memberName,
        String themeName
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getMember().getName(),
                reservation.getTheme().getName()
        );
    }
}

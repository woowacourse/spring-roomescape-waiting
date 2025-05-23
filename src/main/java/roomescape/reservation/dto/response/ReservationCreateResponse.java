package roomescape.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.entity.Reservation;

public record ReservationCreateResponse(
        Long id,
        LocalDate date,
        LocalTime startAt,
        String themeName
) {
    public static ReservationCreateResponse from(Reservation reservation) {
        return new ReservationCreateResponse(
                reservation.getId(),
                reservation.getReservationSlot().getDate(),
                reservation.getReservationSlot().getTime().getStartAt(),
                reservation.getReservationSlot().getTheme().getName()
        );
    }
}

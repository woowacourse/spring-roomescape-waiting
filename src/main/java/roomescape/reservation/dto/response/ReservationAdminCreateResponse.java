package roomescape.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.entity.Reservation;

public record ReservationAdminCreateResponse(
        LocalDate date,
        LocalTime startAt,
        String themeName
) {
    public static ReservationAdminCreateResponse from(Reservation reservation) {
        return new ReservationAdminCreateResponse(
                reservation.getReservationSlot().getDate(),
                reservation.getReservationSlot().getTime().getStartAt(),
                reservation.getReservationSlot().getTheme().getName()
        );
    }
}

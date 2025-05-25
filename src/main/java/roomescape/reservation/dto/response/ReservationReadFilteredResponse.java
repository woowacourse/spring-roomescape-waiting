package roomescape.reservation.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.entity.Reservation;

public record ReservationReadFilteredResponse(
        Long id,
        LocalDate date,
        LocalTime startAt,
        String memberName,
        String themeName
) {
    public static ReservationReadFilteredResponse from(Reservation reservation) {
        return new ReservationReadFilteredResponse(
                reservation.getId(),
                reservation.getReservationSlot().getDate(),
                reservation.getReservationSlot().getTime().getStartAt(),
                reservation.getMember().getName(),
                reservation.getReservationSlot().getTheme().getName()
        );
    }
}

package roomescape.registration.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.registration.reservation.domain.Reservation;

public record ReservationResponse(
        long id,
        String memberName,
        String themeName,
        LocalDate date,
        LocalTime startAt
) {
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(reservation.getId(), reservation.getMember().getName(),
                reservation.getTheme().getName(), reservation.getDate(),
                reservation.getReservationTime().getStartAt());
    }
}

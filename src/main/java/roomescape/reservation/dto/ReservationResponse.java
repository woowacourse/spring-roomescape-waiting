package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;

public record ReservationResponse(
        long id,
        String memberName,
        LocalDate date,
        LocalTime startAt,
        String themeName
) {

    public static ReservationResponse fromReservation(Reservation reservation) {
        return new ReservationResponse(reservation.getId(), reservation.getMemberName(), reservation.getDate(),
                reservation.getReservationTime()
                        .getStartAt(), reservation.getTheme()
                .getName());
    }

}

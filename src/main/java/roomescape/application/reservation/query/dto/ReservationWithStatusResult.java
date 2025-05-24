package roomescape.application.reservation.query.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;

public record ReservationWithStatusResult(
        Long reservationId,
        String themeName,
        LocalDate reservationDate,
        LocalTime reservationTime,
        ReservationStatus status
) {

    public static ReservationWithStatusResult from(Reservation reservation) {
        return new ReservationWithStatusResult(
                reservation.getId(),
                reservation.getTheme().getName(),
                reservation.getDate(),
                reservation.getTime().getStartAt(),
                reservation.getStatus()
        );
    }
}

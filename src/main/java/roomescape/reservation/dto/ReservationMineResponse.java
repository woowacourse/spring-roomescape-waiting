package roomescape.reservation.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationWithStatus;

public record ReservationMineResponse(
        Long reservationId,
        String theme,
        LocalDate date,
        LocalTime time,
        String status
) {
    public static ReservationMineResponse from(final ReservationWithStatus reservationWithStatus) {
        return new ReservationMineResponse(
                reservationWithStatus.getReservation().getId(),
                reservationWithStatus.getReservation().getTheme().getName(),
                reservationWithStatus.getReservation().getDate(),
                reservationWithStatus.getReservation().getTime().getStartAt(),
                reservationWithStatus.getStatusValue()
        );
    }
}

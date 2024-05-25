package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;

public record ReservationViewResponse(
        long reservationId,
        String themeName,
        LocalDate date,
        LocalTime time,
        String status
) {
    public static ReservationViewResponse from(Reservation reservation) {
        ReservationSlot reservationSlot = reservation.getReservationSlot();
        return new ReservationViewResponse(
                reservation.getId(),
                reservationSlot.getTheme().getName(),
                reservationSlot.getDate(),
                reservationSlot.getTime().getStartAt(),
                reservation.getStatus().getStatus());
    }

    public static ReservationViewResponse from(ReservationWithStatus reservationWithStatus) {
        return new ReservationViewResponse(
                reservationWithStatus.reservationId(),
                reservationWithStatus.themeName(),
                reservationWithStatus.date(),
                reservationWithStatus.time(),
                reservationWithStatus.status().getStatus()
        );
    }

    public static ReservationViewResponse from(ReservationWithStatus reservationWithStatus, int waitingCount) {
        return new ReservationViewResponse(
                reservationWithStatus.reservationId(),
                reservationWithStatus.themeName(),
                reservationWithStatus.date(),
                reservationWithStatus.time(),
                waitingCount + "번째 " + reservationWithStatus.status().getStatus()
        );
    }
}

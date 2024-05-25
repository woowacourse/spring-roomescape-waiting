package roomescape.reservation.controller.dto;

import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservationWithStatus(long reservationId, String themeName, LocalDate date, LocalTime time,
                                    ReservationStatus status) {
    public static ReservationWithStatus from(Reservation reservation) {
        return new ReservationWithStatus(
                reservation.getId(),
                reservation.getReservationSlot().getTheme().getName(),
                reservation.getReservationSlot().getDate(),
                reservation.getReservationSlot().getTime().getStartAt(),
                reservation.getStatus());
    }
}
